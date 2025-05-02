package com.vnexos.sema.loader;

/**
 * Custom exception class while handling Loader.
 * 
 * <p>
 * This exception is thrown when errors occur during Loading process, providing
 * specific error messages and optional root cause exceptions for better error
 * handling and debugging.
 * 
 * @author Trần Việt Đăng Quang
 */
public class LoaderException extends Exception {
  /**
   * Constructs a Loader exception with error message
   * 
   * @param msg the message of the error
   */
  public LoaderException(String message) {
    super(message);
  }

  /**
   * Constructs a Loader exception with error message and its cause
   * 
   * @param msg       the message of the error
   * @param throwable the cause of the exception
   */
  public LoaderException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
