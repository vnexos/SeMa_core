package com.vnexos.sema.loader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import com.vnexos.sema.Constants;
import com.vnexos.sema.context.ServerContext;
import com.vnexos.sema.database.Database;
import com.vnexos.sema.database.EntityTable;
import com.vnexos.sema.loader.annotations.AutoWired;
import com.vnexos.sema.loader.annotations.MainClass;
import com.vnexos.sema.loader.interfaces.AModule;
import com.vnexos.sema.util.ClassUtils;
import com.vnexos.sema.util.PrivateServiceConstructor;

/**
 * Handles loading modules by getting all jar files in {@code modules} folder.
 * Then load all the classes inside each of the module files, and store
 * necessary data of each module into the system. The data including
 * controllers, database context, and services.
 * 
 * @author Trần Việt Đăng Quang
 * @see Module
 * @see ServerContext
 * @see EntityTable
 * @see AModule
 * @see Database
 * @see AutoWired
 * @see MainClass
 * @see ClassUtils
 */
public class Loader {
  private static List<Module> moduleClasses = new ArrayList<>();
  private static Module tempModule;
  private static ServerContext context = Constants.context;

  protected static Map<Class<?>, Object> autowiredInstances;
  protected static Queue<Object> queuedInstance;
  protected static List<EntityTable> entityTables;

  /**
   * Private constructor avoid creating an instance of this class
   */
  private Loader() {
  }

  /**
   * Gets the module and its main class.
   * 
   * <p>
   * This method will check if there is no main classes or more than one main
   * classes, and throw errors.
   * 
   * @param jarName     the path to the jar file
   * @param classLoader the class loader of current
   * @param entries     all the entries inside the jar file
   * @return the module represents a jar file inside modules folder
   * @throws LoaderException if there is different than one main class or the path
   *                         of classes in jar file have already been existed
   */
  private static Module getMainClass(String jarName, ClassLoader classLoader, Enumeration<JarEntry> entries)
      throws LoaderException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    // Construct a module
    tempModule = PrivateServiceConstructor.createInstance(
        Module.class,
        PrivateServiceConstructor.createClassTypes(Enumeration.class),
        PrivateServiceConstructor.createObjects(entries));
    tempModule.setModuleFile(jarName);

    // Load all entries inside jar file
    for (String className : tempModule.getClassNames()) {
      // Make sure that the class path has not existed yet
      try {
        Module module = findModule(className);
        if (module != null)
          throw new LoaderException(
              "The class name `" + className + "` has already existed in module `" + module.getModuleName() + "`.");
      } catch (LoaderException ex) {
      }

      Class<?> loadedClass = classLoader.loadClass(className);
      tempModule.add(loadedClass);
      MainClass annotation = loadedClass.getAnnotation(MainClass.class);
      if (loadedClass.getSuperclass() == AModule.class && annotation != null) {
        if (tempModule.getMainClass() == null) {
          // Set main class of module
          tempModule.setMainClass(loadedClass);
          // Set module name that assist much for
          tempModule.setModuleName(annotation.value());
        } else
          throw new LoaderException("`" + jarName + "` contains 2 main classes. Each module must have 1 class",
              new ClassNotFoundException("Main class not found."));
      }
    }

    // Check if there is any main class
    if (tempModule.getMainClass() == null) {
      tempModule = null;
      throw new LoaderException(
          "`" + jarName + "` does not contain any main classes. Each module must have as least 1 class",
          new ClassNotFoundException("Main class not found."));
    }
    return tempModule;
  }

  /**
   * Handles with all fields that annotated by {@code &#64;AutoWired}, by assign
   * the instance associated to the type to the field.
   * 
   * @param clazz    the type to handle
   * @param instance the instance of the class that contains the field
   * @return true it the instance is not null, false otherwise
   * @throws IllegalArgumentException if the field is not accessible
   * @throws IllegalAccessException   if the field is not accessible
   */
  public static boolean handleAutoWiredFields(Class<?> clazz, Object instance)
      throws IllegalArgumentException, IllegalAccessException {
    List<Field> fields = ClassUtils.getAllFields(clazz);
    for (Field field : fields) {
      if (field.getAnnotation(AutoWired.class) != null) {
        field.setAccessible(true);
        Object obj = autowiredInstances.get(field.getType());

        if (obj == null)
          return false;
        field.set(instance, obj);
      }
    }
    return true;
  }

  /**
   * Loads the jar file into Module.
   * 
   * @param jar         the jar file to process
   * @param classLoader the class loader of current project
   * @return the module that contains all data about jar file
   */
  private static Module loadJar(File jar, ClassLoader classLoader) {
    try (JarFile jarFile = new JarFile(jar)) {
      // Get all entries in jar file
      Enumeration<JarEntry> entries = jarFile.entries();

      return getMainClass(jar.getName(), classLoader, entries);
    } catch (IOException | ClassNotFoundException | InvocationTargetException | IllegalAccessException
        | IllegalArgumentException | InstantiationException | NoSuchMethodException e) {
      context.log(e);
    } catch (LoaderException e) {
      context.log(e);
    }
    return null;
  }

  /**
   * Get all files in library directory
   * 
   * @param libFoler directory to get files
   * @return the list of all files in directory tree
   */
  private static List<File> getFileTree(File libFoler) {
    List<File> files = new ArrayList<>();

    for (File f : libFoler.listFiles()) {
      if (f.isDirectory()) {
        files.addAll(getFileTree(f));
      } else {
        files.add(f);
      }
    }

    return files;
  }

  /**
   * Get URLs of all modules and libraries
   * 
   * @param subFiles list of files in module folder
   * @return the list of JAR file URL
   */
  private static URL[] getLibUrls(File[] subFiles) {
    List<URL> urls = new ArrayList<>();
    File file = new File(context.joinPath("libs"));

    if (!file.exists())
      file.mkdirs();

    List<File> libFiles = getFileTree(file);
    File[] filesToCatch = Stream.concat(libFiles.stream(), Arrays.stream(subFiles)).toArray(File[]::new);

    for (File f : filesToCatch) {
      if (f.getName().toLowerCase().endsWith(".jar")) {
        try {
          urls.add(f.toURI().toURL());
        } catch (MalformedURLException e) {
          context.log(e);
        }
      }
    }

    return urls.toArray(new URL[0]);
  }

  /**
   * Initializes the loader. This method will get a list of jar file inside
   * modules folder and analyze the jar file to get necessary data for controlling
   * and processing a module.
   */
  public static void init() {
    autowiredInstances = new HashMap<>();
    queuedInstance = new LinkedList<>();
    entityTables = new ArrayList<>();
    // Get path of module folder
    String path = context.joinPath(Constants.getString("module.folder"));
    File file = new File(path);

    // Create modules folder it does not exist
    if (!file.exists())
      file.mkdirs();

    // Load all jar file as a module
    File[] subFiles = file.listFiles();
    // Get current loader
    URLClassLoader classLoader = new URLClassLoader(
        getLibUrls(subFiles),
        Loader.class.getClassLoader());
    Thread.currentThread().setContextClassLoader(classLoader);

    for (File subFile : subFiles) {
      if (subFile.getName().toLowerCase().endsWith(".jar")) {
        // Get module information
        Module module = loadJar(subFile, classLoader);

        if (module != null)
          moduleClasses.add(module);
      }
    }
  }

  /**
   * Loads all modules components and handle with auto wired fields inside each
   * modules, then load all entities inside modules and enable all plugins.
   * 
   * @throws SQLException if the entity cannot be proccess
   */
  public static void loadPlugins() throws SQLException {
    try {
      for (Module moduleClass : moduleClasses) {
        moduleClass.preEnable();
        if (!handleAutoWiredFields(moduleClass.getMainClass(), moduleClass.getModuleMainClass()))
          queuedInstance.add(moduleClass.getModuleMainClass());
      }

      while (!queuedInstance.isEmpty()) {
        Object obj = queuedInstance.poll();
        if (!handleAutoWiredFields(obj.getClass(), obj))
          queuedInstance.add(obj);
      }
      Database.loadDatabase(entityTables);

      for (Module moduleClass : moduleClasses) {
        moduleClass.onEnabled();
      }
    } catch (IllegalArgumentException | IllegalAccessException e) {
      context.log(e);
    }
  }

  /**
   * Disable all modules.
   */
  public static void unloadPlugins() {
    for (Module moduleClass : moduleClasses) {
      moduleClass.onDisabled();
    }
  }

  /**
   * Get max length of module name for nice printing.
   * 
   * @return the length of longest module name
   */
  public static int getMaxModuleNameLength() {
    int res = 0;
    for (Module module : moduleClasses) {
      int length = module.getModuleName().length();
      if (length > res)
        res = length;
    }
    return res;
  }

  /**
   * Looks for module that contain class name, that why the class path cannot be
   * duplicated.
   * 
   * @param className the path of class
   * @return the module which contains class that have the same class name as
   *         given class name
   * @throws LoaderException if the module cannot be found
   */
  public static Module findModule(String className) throws LoaderException {
    for (Module module : moduleClasses) {
      if (module.containsClass(className))
        return module;
    }
    throw new LoaderException("Your module may not be loaded!");
  }
}
