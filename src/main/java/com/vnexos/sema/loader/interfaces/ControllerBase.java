package com.vnexos.sema.loader.interfaces;

import java.util.Map;

import com.vnexos.sema.ApiResponse;

/**
 * Represents a controller. All controllers must inherit from this class.
 * 
 * <p>
 * This class contains all needed methods for controller.
 * 
 * @author Trần Việt Đăng Quang
 * @see ApiResponse
 */
public class ControllerBase {
  private Map<String, String> headers;

  protected String getHeader(String name) {
    return headers.get(name);
  }

  protected Map<String, String> getHeaders() {
    return headers;
  }

  protected <T> ApiResponse<T> createOk(T obj) {
    return new ApiResponse<T>(obj, 200);
  }

  protected <T> ApiResponse<T> createBadRequest(T obj) {
    return new ApiResponse<T>(obj, 400);
  }

  protected <T> ApiResponse<T> createInternalRequest(T obj) {
    return new ApiResponse<T>(obj, 500);
  }

  protected <T> ApiResponse<T> custom(T obj, int statusCode) {
    return new ApiResponse<T>(obj, 200);
  }
}
