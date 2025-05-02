package com.vnexos.sema;

/**
 * Represents a response from a route of controller.
 * 
 * @author Trần Việt Đăng Quang
 */
public class ApiResponse<T> {
  private T data; // Generic type for the response data
  private int statusCode; // HTTP status code
  private long timestamp; // Response timestamp
  private String contentType; // Response type

  /**
   * Constructs an instance of ApiResponse.
   */
  public ApiResponse() {
    this.statusCode = 200;
    this.timestamp = System.currentTimeMillis();
    this.contentType = "application/json";
  }

  /**
   * Constructs an instance of ApiResponse with data and status code.
   * 
   * @param data       the data of response
   * @param statusCode the status code of the response
   */
  public ApiResponse(T data, int statusCode) {
    this();
    this.data = data;
    this.statusCode = statusCode;
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * Gets data of response.
   * 
   * @return the response data
   */
  public T getData() {
    return data;
  }

  /**
   * Sets data of response.
   * 
   * @param data the data to set.
   */
  public void setData(T data) {
    this.data = data;
  }

  /**
   * Gets JSON value of data
   * 
   * @return the parsed JSON from data.
   */
  public String getJsonData() {
    return Constants.gson.toJson(data);
  }

  /**
   * Gets the response status code.
   * 
   * @return status code of response.
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Sets status code for the response.
   * 
   * @param statusCode the status code to set
   */
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * Gets current timestamp of response.
   * 
   * @return timestamp of response
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets current timestamp of response.
   * 
   * @param timestamp the timestamp to set
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Sets content type of response.
   * 
   * @param contentType the content type to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * Gets content type of response.
   * 
   * @return the response content type
   */
  public String getContentType() {
    return contentType;
  }
}