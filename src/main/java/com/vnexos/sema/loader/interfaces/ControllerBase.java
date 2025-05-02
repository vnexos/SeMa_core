package com.vnexos.sema.loader.interfaces;

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
  public <T> ApiResponse<T> createOk(T obj) {
    return new ApiResponse<T>(obj, 200);
  }

  public <T> ApiResponse<T> createBadRequest(T obj) {
    return new ApiResponse<T>(obj, 400);
  }

  public <T> ApiResponse<T> createInternalRequest(T obj) {
    return new ApiResponse<T>(obj, 500);
  }

  public <T> ApiResponse<T> custom(T obj, int statusCode) {
    return new ApiResponse<T>(obj, 200);
  }
}
