package com.vnexos.sema.loader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

import com.vnexos.sema.ApiController;
import com.vnexos.sema.Constants;
import com.vnexos.sema.context.ModuleServerContext;
import com.vnexos.sema.context.ServerContext;
import com.vnexos.sema.database.EntityTable;
import com.vnexos.sema.database.annotations.Entity;
import com.vnexos.sema.database.helpers.DatabaseContext;
import com.vnexos.sema.loader.annotations.Controller;
import com.vnexos.sema.loader.annotations.HttpDelete;
import com.vnexos.sema.loader.annotations.HttpGet;
import com.vnexos.sema.loader.annotations.HttpPatch;
import com.vnexos.sema.loader.annotations.HttpPost;
import com.vnexos.sema.loader.annotations.HttpPut;
import com.vnexos.sema.loader.annotations.HttpRoute;
import com.vnexos.sema.loader.annotations.Service;
import com.vnexos.sema.loader.interfaces.AModule;
import com.vnexos.sema.util.ClassUtils;
import com.vnexos.sema.util.PrivateServiceConstructor;

/**
 * Contains and handles all module processing. This class completely represents
 * a jar file module.
 * 
 * @author Trần Việt Đăng Quang
 * @see AModule
 * @see ServerContext
 */
public class Module {
  private Class<?> mainClass;
  private List<Class<?>> classList;
  private List<String> classNames;

  private String moduleName;
  private String moduleFile;
  private ServerContext context = Constants.context;
  private AModule moduleMainClass;
  private ClassLoader classLoader;
  private boolean isEnabled = false;

  /**
   * Gets class name associated with entry
   * 
   * @param entry the entry of jar file
   * @return the class name
   */
  private static String getClassNameFromEntry(JarEntry entry) {
    return entry.getName()
        .replace('/', '.') // Convert path to package notation
        .replace(".class", ""); // Remove .class extension
  }

  /**
   * Constructs a Module by Jar entries. The constructor is private to avoid
   * creating an instance of module.
   * 
   * @param entries all entries in jar file.
   */
  private Module(Enumeration<JarEntry> entries) {
    classList = new ArrayList<>();
    classNames = new ArrayList<>();

    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if (entry.getName().endsWith(".class"))
        classNames.add(getClassNameFromEntry(entry));
    }
  }

  /**
   * Gets the main class of the module.
   * 
   * @return the stored main class
   */
  public Class<?> getMainClass() {
    return mainClass;
  }

  /**
   * Sets the main class of the module as well as gets the main class as the
   * instance of {@code AModule}.
   * 
   * @param mainClass the main class to be stored.
   */
  public void setMainClass(Class<?> mainClass) {
    this.mainClass = mainClass;
    try {
      moduleMainClass = getModule();
    } catch (LoaderException e) {
      context.log(e);
    }
  }

  /**
   * Gets name of module.
   * 
   * @return the module name
   */
  public String getModuleName() {
    return moduleName;
  }

  /**
   * Sets the name of current module.
   * 
   * @param moduleName the name to set
   */
  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  /**
   * Adds class into a module class list.
   * 
   * @param clazz the class to add
   */
  public void add(Class<?> clazz) {
    classList.add(clazz);
  }

  /**
   * Get class at the index.
   * 
   * @param index the index of class in class list
   * @return the class at the index
   */
  public Class<?> get(int index) {
    return classList.get(index);
  }

  /**
   * Gets the jar file of module.
   * 
   * @return the file path of module
   */
  public String getModuleFile() {
    return moduleFile;
  }

  /**
   * Sets the file of module
   * 
   * @param moduleFile the path of file to set
   */
  public void setModuleFile(String moduleFile) {
    this.moduleFile = moduleFile;
  }

  /**
   * Sets class loader for modules
   * 
   * @param classLoader
   */
  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Gets class loader of module
   * 
   * @return the class loader
   */
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  /**
   * Get all class names of a module.
   * 
   * @return the list of class names
   */
  public List<String> getClassNames() {
    return classNames;
  }

  /**
   * Get main class as a {@code AModule} instance.
   * 
   * @return the instance of {@code AModule}
   * @throws LoaderException if the main class does not have a default constructor
   *                         or the constructor cannot be accessed
   */
  private AModule getModule() throws LoaderException {
    try {
      Constructor<?> constructor = mainClass.getConstructor();
      AModule mod = (AModule) constructor.newInstance();

      return mod;
    } catch (SecurityException | NoSuchMethodException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new LoaderException("The `" + moduleFile + "` contains an invalid main class.", e);
    }
  }

  /**
   * Gets super the annotation annotated to the given annotation.
   * 
   * @param annotation annotation to process
   * @return the HttpRoute annotation
   */
  private Annotation getSuperAnnotation(Annotation annotation) {
    return annotation.annotationType().getAnnotation(HttpRoute.class);
  }

  /**
   * Gets instance of {@code AModule}.
   * 
   * @return the {@code AModule} instance
   */
  public AModule getModuleMainClass() {
    return moduleMainClass;
  }

  /**
   * Gets all annotations annotated with the HttpMethod.
   * 
   * @param method the method to get
   * @return the list of annotations
   */
  private List<Annotation> getRouteAnnotation(Method method) {
    List<Annotation> annotations = new ArrayList<>();
    for (Annotation annotation : method.getAnnotations()) {
      if (getSuperAnnotation(annotation) != null)
        annotations.add(annotation);
    }
    return annotations;
  }

  /**
   * Handles loading controller inside a module.
   * 
   * @param clazz      the class represents controller
   * @param controller the {@code &#64;Controller} annotation
   * @throws ApiException if an error occurs while loading a Controller
   */
  private void handleLoadingController(Class<?> clazz, Controller controller) throws ApiException {
    Object instance;
    try {
      instance = clazz.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new ApiException("Controller must have only default constructor!", e);
    }
    String path = controller.value().startsWith("/")
        ? controller.value()
        : Route.join(Constants.getString("module.api-prefix"), controller.value());
    for (Method method : clazz.getDeclaredMethods()) {
      String routePath = "";
      List<Annotation> annotations = getRouteAnnotation(method);
      if (annotations.size() > 0)
        for (Annotation annotation : annotations) {

          try {
            if (!Loader.handleAutoWiredFields(clazz, instance))
              Loader.queuedInstance.add(instance);
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ApiException("Error loading auto wired field!", e);
          }

          HttpRoute httpRoute = (HttpRoute) getSuperAnnotation(annotation);
          switch (httpRoute.value()) {
            case GET:
              routePath = ((HttpGet) annotation).value().startsWith("/")
                  ? ((HttpGet) annotation).value()
                  : Route.join(path, ((HttpGet) annotation).value());
              break;
            case POST:
              routePath = ((HttpPost) annotation).value().startsWith("/")
                  ? ((HttpPost) annotation).value()
                  : Route.join(path, ((HttpPost) annotation).value());
              break;
            case PUT:
              routePath = ((HttpPut) annotation).value().startsWith("/")
                  ? ((HttpPut) annotation).value()
                  : Route.join(path, ((HttpPut) annotation).value());
              break;
            case PATCH:
              routePath = ((HttpPatch) annotation).value().startsWith("/")
                  ? ((HttpPatch) annotation).value()
                  : Route.join(path, ((HttpPatch) annotation).value());
              break;
            case DELETE:
              routePath = ((HttpDelete) annotation).value().startsWith("/")
                  ? ((HttpDelete) annotation).value()
                  : Route.join(path, ((HttpDelete) annotation).value());
              break;
          }
          ApiController.addApi(routePath, httpRoute.value(), method, instance);
        }
    }
  }

  /**
   * Handles loading an entity.
   * 
   * @param clazz  the class that represent a database entity and be annotated
   *               by the {@code &#64;Entity} annotation
   * @param entity the instance of Entity annotation
   */
  private void handleLoadingEntity(Class<?> clazz, Entity entity) {
    Loader.entityTables.add(new EntityTable(clazz, entity));
  }

  private void handleLoadingContext(Class<?> clazz) {
    Type[] types = clazz.getGenericInterfaces();
    for (Type type : types) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      if (parameterizedType.getRawType() == DatabaseContext.class) {
        Loader.autowiredInstances.put(clazz, Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class[] { clazz },
            // This lambda replace the InvocationHandler class
            (proxy, method, args) -> ContextHandler.invoke(
                proxy,
                method,
                args == null ? new Object[0] : args,
                parameterizedType.getActualTypeArguments())));
      }
    }
  }

  /**
   * Handles loading a service.
   * 
   * @param clazz a service class which annotated by the {@code &#64;Service}
   *              annotation
   * @throws ApiException if an error occurs while loading service.
   */
  private void handleLoadingService(Class<?> clazz) throws ApiException {
    Object instance;
    try {
      instance = clazz.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new ApiException("Service must have only default constructor!", e);
    }
    Loader.autowiredInstances.put(clazz, instance);
    try {
      if (!Loader.handleAutoWiredFields(clazz, instance))
        Loader.queuedInstance.add(instance);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ApiException("Error loading auto wired field!", e);
    }
  }

  /**
   * Handles pre-processing the module
   */
  public void preEnable() {
    try {
      for (Class<?> clazz : classList) {
        Annotation annotation;

        if ((annotation = clazz.getAnnotation(Controller.class)) != null) {
          handleLoadingController(clazz, (Controller) annotation);
        } else if ((annotation = clazz.getAnnotation(Entity.class)) != null) {
          handleLoadingEntity(clazz, (Entity) annotation);
        } else if (ClassUtils.checkClassesContain(clazz.getInterfaces(), DatabaseContext.class)) {
          handleLoadingContext(clazz);
        } else if (clazz.getAnnotation(Service.class) != null) {
          handleLoadingService(clazz);
        }
      }
    } catch (ApiException e) {
      context.log(e);
    }
  }

  /**
   * Enables the module by constructing a context and call to the enable module.
   */
  public void onEnabled() {
    if (!isEnabled) {
      ModuleServerContext context;
      try {
        context = PrivateServiceConstructor.createInstance(
            ModuleServerContext.class,
            PrivateServiceConstructor.createClassTypes(ServerContext.class, Module.class),
            PrivateServiceConstructor.createObjects(Constants.context, this));

        moduleMainClass.onEnabled(context);
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
          | IllegalArgumentException | InvocationTargetException e) {
        Constants.context.log(e);
      }
      isEnabled = true;
    }
  }

  /**
   * Disables the module.
   */
  public void onDisabled() {
    if (isEnabled) {
      moduleMainClass.onDisabled();
      isEnabled = false;
    }
  }

  /**
   * Check if the module contain the given class path
   * 
   * @param classPath the path of class to check
   * @return true if the module contains any class
   */
  public boolean containsClass(String classPath) {
    for (String className : classNames) {
      if (className.equals(classPath))
        return true;
    }
    return false;
  }
}
