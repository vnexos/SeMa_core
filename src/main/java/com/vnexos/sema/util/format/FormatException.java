package com.vnexos.sema.util.format;

/**
 * Handles exception when format error.
 * 
 * @author Trần Việt Đăng Quang
 */
public class FormatException extends Exception {
  private static final long serialVersionUID = -8437428502517486885L;

  public FormatException(String message) {
    super(message);
  }
}
