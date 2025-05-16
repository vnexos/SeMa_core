package com.vnexos.sema.loader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.vnexos.sema.ApiResponse;
import com.vnexos.sema.Constants;
import com.vnexos.sema.loader.annotations.FromBody;
import com.vnexos.sema.loader.annotations.FromQuery;
import com.vnexos.sema.loader.annotations.FromRoute;
import com.vnexos.sema.loader.interfaces.ControllerBase;
import com.vnexos.sema.util.ClassUtils;
import com.vnexos.sema.util.Mapper;

/**
 * Represents a Route of a Controller.
 * <p>
 * This class also implement the way to interact with the route.
 * 
 * @author Trần Việt Đăng Quang
 */
public class Route {
  private static final Gson gson = Constants.gson;
  private HttpMethod httpMethod;
  private String route;
  private Method method;
  private List<String> parts;
  private Object instance;

  /**
   * Extracts the route into small parts.
   * 
   * @param route the route to be analyzed
   * @return the list of parts in route
   */
  private static List<String> analyzeRoute(String route) {
    return Arrays.stream(route.substring(1).split("/"))
        .collect(Collectors.toList());
  }

  /**
   * Constructs a route.
   * 
   * @param httpMethod the method of a routes
   * @param route      the path of the route
   * @param method     the method that handling the route
   * @param instance   the instance of Controller
   */
  public Route(HttpMethod httpMethod, String route, Method method, Object instance) {
    this.httpMethod = httpMethod;
    this.route = route;
    this.method = method;
    this.parts = analyzeRoute(route);
    this.instance = instance;
  }

  /**
   * Gets the method of the route.
   * 
   * @return the HTTP method which supported by the current route
   */
  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  /**
   * Sets the method for the current route.
   * 
   * @param httpMethod the HTTP method to set
   */
  public void setHttpMethod(HttpMethod httpMethod) {
    this.httpMethod = httpMethod;
  }

  /**
   * Gets the path of the route.
   * 
   * @return path of the current route.
   */
  public String getRoute() {
    return route;
  }

  /**
   * Sets the path of the route
   * 
   * @param route the path of route to set
   */
  public void setRoute(String route) {
    this.route = route;
    this.parts = analyzeRoute(route);
  }

  /**
   * Checks if the current route match the given route. This method also processes
   * the part of a route for {@code &#123;**&#125;} and store it into a Map.
   * 
   * @param route the route to analyze
   * @return a map of param if the current route match with the given route, empty
   *         otherwise
   */
  public Optional<Map<String, String>> checkRoute(String route) {
    List<String> currentRoute = analyzeRoute(route);
    Map<String, String> paramMap = new HashMap<>();

    // make sure 2 paths have the same size
    if (currentRoute.size() != parts.size())
      return Optional.empty();

    // map param with value
    for (int i = 0; i < parts.size(); i++) {
      String part = parts.get(i);
      String desPart = currentRoute.get(i);
      if (part.matches("\\{.*?\\}"))
        paramMap.put(part.replaceAll("[\\{\\}]", ""), desPart);
      else if (!part.equals(desPart))
        return Optional.empty();
    }
    return Optional.of(paramMap);
  }

  /**
   * Invokes the method that handling the route.
   * 
   * @param param the param map of a route
   * @param query the query map of a route
   * @param body  the body in the request
   * @param parts the parts given in the {@code multipart} content type
   * @return the response returned from the route method after invoke
   * @throws ApiException if an error occurs while processing a route
   */
  public ApiResponse<?> invoke(Map<String, String> param, Map<String, String> query, String body, List<Part> parts,
      Map<String, String> headers)
      throws ApiException {
    final Method method = this.method;
    List<Object> paramValues = new ArrayList<>();

    // Process all param of route method
    for (Parameter parameter : method.getParameters()) {
      if (parameter.getAnnotations().length > 1)
        continue;
      if (parameter.getAnnotation(FromRoute.class) != null) {
        paramValues.add(gson.fromJson(param.get(parameter.getName()), parameter.getType()));
      } else if (parameter.getAnnotation(FromQuery.class) != null) {
        if (ClassUtils.isPrimitive(parameter.getType())) {
          paramValues.add(gson.fromJson(query.get(parameter.getName()), parameter.getType()));
        } else if (parameter.getType() == String.class) {
          paramValues.add(query.get(parameter.getName()));
        } else
          paramValues.add(Mapper.map(query, parameter.getType()));
      } else if (parameter.getAnnotation(FromBody.class) != null) {
        if (parameter.getType() == Part.class) {
          List<Part> p = parts.stream().filter(
              part -> part.getName().equals(parameter.getName())).collect(Collectors.toList());
          if (p.size() != 1)
            throw new ApiException("Invalid part parameter");

          paramValues.add(p.get(0));
        } else if (!ClassUtils.isPrimitive(parameter.getType()) && parameter.getType() != String.class)
          try {

            paramValues.add(Mapper.map(body, parameter.getType()));
          } catch (JsonSyntaxException ex) {
            throw new ApiException("Invalid json syntax", ex);
          }
        else
          paramValues.add(null);
      } else
        paramValues.add(null);
    }

    // Invoke method and get ApiResponse
    Object res;
    try {
      Class<?> controllerBase = instance.getClass().getSuperclass();
      if (controllerBase == ControllerBase.class) {
        Field field = controllerBase.getDeclaredField("headers");
        field.setAccessible(true);
        field.set(instance, headers);
      }

      res = method.invoke(instance, paramValues.toArray());
    } catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException | SecurityException e) {
      throw new ApiException("Cannot invoke route method", e);
    }
    return (ApiResponse<?>) res;
  }

  /**
   * Gets the method that handling route
   * 
   * @return the route method
   */
  public Method getMethod() {
    return method;
  }

  /**
   * Join all URL parts
   * 
   * @param base   the base URL
   * @param params the parts to join into URL path
   * @return the URL path after joined
   */
  public static String join(String base, String... params) {
    Path path = Paths.get(base.equals("/") ? "" : base, params);
    if (path.toString().length() == 0)
      return "/";
    return (base.equals("/") ? "/" : "") + path.toString().replace("\\", "/");
  }
}
