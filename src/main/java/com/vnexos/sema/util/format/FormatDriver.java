package com.vnexos.sema.util.format;

/**
 * Driver for formatting log message.
 * 
 * <p>
 * This driver handles background, foreground, etc. for logging to console. For
 * example:
 * 
 * <pre>
 * "$fg(15)Hello World!"
 * </pre>
 * 
 * The string {@code "Hello World!"} will be formatted at a pure white when
 * print it to console.
 * 
 * @author Trần Việt Đăng Quang
 */
public class FormatDriver {
  private static final char ESCAPE = 0x1b;

  // Text color
  public String fg(int color) {
    return ESCAPE + "[38;5;" + color + "m";
  }

  // Background color
  public String bg(int color) {
    return ESCAPE + "[48;5;" + color + "m";
  }

  // Reset all format
  public String reset() {
    return ESCAPE + "[0m";
  }

  // Bold text
  public String bold() {
    return ESCAPE + "[1m";
  }

  // Italic text
  public String italic() {
    return ESCAPE + "[2m";
  }

  // Underline text
  public String underline() {
    return ESCAPE + "[4m";
  }
}
