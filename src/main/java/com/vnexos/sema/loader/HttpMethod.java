package com.vnexos.sema.loader;

/**
 * Enumerates the supported HTTP method.
 * 
 * @author Trần Việt Đăng Quang
 */
public enum HttpMethod {
  GET("GET"),
  POST("POST"),
  PUT("PUT"),
  PATCH("PATCH"),
  DELETE("DELETE");

  private final String value;

  private HttpMethod(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    String name = getClass().getName();
    name = name.substring(name.lastIndexOf('.'));
    return name + "." + value;
  }
}
