package com.vnexos.sema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vnexos.sema.loader.ApiException;
import com.vnexos.sema.loader.HttpMethod;
import com.vnexos.sema.loader.Route;
import com.vnexos.sema.util.Mapper;
import com.vnexos.sema.util.PrivateServiceConstructor;
import com.vnexos.sema.util.StringUtils;
import com.vnexos.sema.util.format.FormatException;
import com.vnexos.sema.util.format.Formatter;
import com.vnexos.sema.util.logger.Logger;
import com.vnexos.sema.util.logger.LoggerFormatDriver;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * Handles all incoming requests.
 * 
 * @author Trần Việt Đăng Quang
 */
@MultipartConfig
public class ApiController extends HttpServlet {
  private static final long serialVersionUID = -8740548020982623621L;
  private static final List<Route> apis;

  // For cors
  private static String[] origins;
  private static String methods;
  private static String headers;
  private static int maxAge;
  private static boolean credentials;

  static {
    apis = new ArrayList<>();
    origins = Constants.getString("cors.origin").split("\\|");
    methods = Constants.getString("cors.method").replaceAll("\\|", ", ");
    headers = Constants.getString("cors.header").replaceAll("\\|", ", ");
    maxAge = Constants.getInteger("cors.max-age");
    credentials = Constants.getBoolean("cors.credentials");
  }

  /**
   * Logs the route after request.
   * 
   * @param port        the status code of route
   * @param route       the path of route
   * @param queryString the query string
   * @param method      the HTTP method of route
   */
  private static void logRoute(int port, String route, String queryString, String method, long time) {
    StringBuilder msg = new StringBuilder();

    // Add method and route with the white color
    msg
        .append("$fg(15)")
        .append(method)
        .append(' ')
        .append(route);
    // Add querystring if it is not null
    if (queryString != null)
      msg
          .append('?')
          .append(queryString);

    msg.append(' ');

    // Add status color and status value
    msg.append("$fg(");
    if (port >= 100 && port < 200)
      msg.append(15);
    else if (port >= 200 && port < 300)
      msg.append(10);
    else if (port >= 300 && port < 400)
      msg.append(12);
    else if (port >= 400 && port < 500)
      msg.append(11);
    else if (port >= 500 && port < 600)
      msg.append(01);
    else
      msg.append(242);
    msg.append(')');
    msg.append(port);
    msg.append("$fg(15) (");
    msg.append("$fg(12)");
    msg.append(System.currentTimeMillis() - time);
    msg.append(" $fg(15)ms)");

    // Format the message of route.
    Formatter formatter = new Formatter();
    try {
      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
      LoggerFormatDriver.setClassPath(elements[2]);
      PrivateServiceConstructor.invokeFunction(
          Logger.class, "route", Constants.context.getLogger(),
          PrivateServiceConstructor.createClassTypes(String.class),
          PrivateServiceConstructor.createObjects(formatter.format(msg.toString())));
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException
        | FormatException e) {
      Constants.context.log("ROUTE: " + msg);
    }
  }

  /**
   * Finds the route that matches the given route and HTTP method.
   * 
   * @param endpoint   the path of route
   * @param httpMethod the method of route
   * @return the matched route
   */
  private static final Route findRoute(String endpoint, HttpMethod httpMethod) {
    for (Route route : apis)
      if (route.checkRoute(endpoint).isPresent() && route.getHttpMethod() == httpMethod)
        return route;
    return null;
  }

  /**
   * Add route to the list
   * 
   * @param endpoint   the path of route
   * @param httpMethod the HTTP method of route
   * @param method     the handling method of route
   * @param instance   the instance of constroller which contains handling method
   * @throws ApiException if an error occurs while processing route
   */
  public static final void addApi(String endpoint, HttpMethod httpMethod, Method method, Object instance)
      throws ApiException {
    if (findRoute(endpoint, httpMethod) == null)
      apis.add(new Route(httpMethod, endpoint, method, instance));
    else
      throw new ApiException("Route `" + endpoint + "` with `" + httpMethod + "` method has already existed.");
  }

  /**
   * Handles CORS.
   * 
   * @param req  the request of server
   * @param resp the response of server
   * @return false if the CORS failed, true otherwise
   */
  private boolean handleCors(HttpServletRequest req, HttpServletResponse resp) {
    String origin = req.getHeader("Origin");

    if (Arrays.stream(origins).anyMatch(o -> o.equals(origin))) {
      resp.setHeader("Access-Control-Allow-Origin", origin);
      resp.setHeader("Access-Control-Allow-Credentials", Boolean.toString(credentials));
    }
    resp.setHeader("Access-Control-Allow-Methods", methods);
    resp.setHeader("Access-Control-Allow-Headers", headers);
    resp.setHeader("Access-Control-Max-Age", Integer.toString(maxAge));
    return origin != null || Constants.getBoolean("module.development");
  }

  /**
   * Converts to the body and parts of multi part.
   * 
   * @param req      the request of server
   * @param filePart the list to store part get from request
   * @return the parsed body in JSON
   * @throws IOException      if reading body failed
   * @throws ServletException if getting parts failed
   */
  private String parseRequestBody(HttpServletRequest req, List<Part> filePart) throws IOException, ServletException {
    String contentType = req.getContentType();
    if (contentType != null && contentType.startsWith("multipart/form-data")) {
      Map<String, String> data = new HashMap<>();
      for (Part part : req.getParts()) {
        if (part.getContentType() == null) {
          data.put(part.getName(), readPart(part));
        } else {
          filePart.add(part);
        }
      }
      return Mapper.map(data, String.class);
    } else {
      return readBody(req.getReader());
    }
  }

  /**
   * Reads body from reader of request.
   * 
   * @param reader the request reader
   * @return the parsed body in JSON
   * @throws IOException if reading lines of reader failed
   */
  private String readBody(BufferedReader reader) throws IOException {
    StringBuilder body = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      body.append(line).append("\n");
    }
    return body.toString();
  }

  /**
   * Reads information inside a part
   * 
   * @param part the part to read
   * @return the line get from part
   * @throws IOException if reading from part failed
   */
  private String readPart(Part part) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()))) {
      StringBuilder result = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        result.append(line).append("\n");
      }
      return result.toString().trim();
    }
  }

  /**
   * Write response with data in the {@code ApiResponse} object.
   * 
   * @param resp     the response of server
   * @param response the response get after processing route method
   * @throws IOException if writer from response cannot be get
   */
  private void writeResponse(HttpServletResponse resp, ApiResponse<?> response) throws IOException {
    resp.setContentType(response.getContentType());
    resp.setStatus(response.getStatusCode());
    if (response.getContentType().startsWith("text/html")) {
      resp.getWriter().write(response.getData().toString());
    } else if (response.getContentType().startsWith("application/json")) {
      resp.getWriter().write(response.getJsonData());
    } else {
      OutputStream os = resp.getOutputStream();
      os.write(response.getBinaryData());
      resp.setContentLengthLong(response.getBinaryData().length);
    }
  }

  /**
   * Handle all paths and methods.
   */
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    long time = System.currentTimeMillis();
    boolean isOriginFailed = !handleCors(req, resp);

    String path = req.getRequestURI();
    String method = req.getMethod();
    Map<String, String> query = StringUtils.queryToMap(req.getQueryString());
    List<Part> filePart = new ArrayList<>();
    String body = parseRequestBody(req, filePart);
    HttpMethod httpMethod = HttpMethod.valueOf(method);

    // Get all headers from request
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = req.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      headers.put(headerName, req.getHeader(headerName));
    }

    try {
      if (isOriginFailed) {
        resp.getWriter().write("{\"msg\": \"CORS failed!\"}");
        resp.setStatus(400);
        return;
      }
      Route route = findRoute(path, httpMethod);
      if (route != null) {
        Map<String, String> pathParams = route.checkRoute(path).get();
        ApiResponse<?> response = route.invoke(
            pathParams, query, body,
            filePart.stream()
                .map(part -> {
                  try {
                    return new com.vnexos.sema.loader.Part(part);
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                })
                .collect(Collectors.toList()),
            headers);
        writeResponse(resp, response);
      } else {
        resp.setStatus(404);
        resp.getWriter().write("{\"msg\": \"Not found!\"}");
      }
    } catch (ApiException | IOException e) {
      Constants.context.log(e);
      resp.setStatus(500);
      resp.getWriter().write("{\"msg\": \"Internal server error!\"}");
    } catch (Exception e) {
      resp.setStatus(500);
      resp.getWriter().write("{\"msg\": \"Internal server error!\"}");
      Constants.context.log(e);
    }

    logRoute(resp.getStatus(), path, req.getQueryString(), method, time);
  }
}
