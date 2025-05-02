package com.vnexos.sema.loader;

/**
 * Custom exception class while handling Database context.
 * 
 * <p>
 * This exception is thrown when errors occur during Database context handling,
 * providing specific error messages and optional root cause exceptions for
 * better error handling and debugging.
 * 
 * @author Trần Việt Đăng Quang
 */
public class ContextException extends Exception {
  /**
   * Constructs an API exception with error message
   * 
   * @param msg the message of the error
   */
  public ContextException(String msg) {
    super(msg);
  }

  /**
   * Constructs an API exception with error message and its cause
   * 
   * @param msg       the message of the error
   * @param throwable the cause of the exception
   */
  public ContextException(String msg, Throwable throwable) {
    super(msg, throwable);
  }
}
