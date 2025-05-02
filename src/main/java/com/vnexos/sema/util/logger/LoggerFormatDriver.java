package com.vnexos.sema.util.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.vnexos.sema.Constants;
import com.vnexos.sema.loader.Loader;
import com.vnexos.sema.util.format.FormatException;
import com.vnexos.sema.util.format.Formatter;

/**
 * Driver for formatting logger.
 * 
 * @author Trần Việt Đăng Quang
 */
public class LoggerFormatDriver {
  private static LoggerType type;
  private static StackTraceElement classPath = null;
  private static boolean keepClassPath = false;
  private static String module = null;

  /**
   * Sets module for tracking on while logging.
   * 
   * @param module the module name
   */
  public static void setModule(String module) {
    LoggerFormatDriver.module = module;
  }

  /**
   * Sets type of logging.
   * 
   * @param type the logging level
   */
  public static void setType(LoggerType type) {
    LoggerFormatDriver.type = type;
  }

  /**
   * Sets the stack trace at the position calling the log function.
   * 
   * @param classPath the path of class
   */
  public static void setClassPath(StackTraceElement classPath) {
    LoggerFormatDriver.classPath = classPath;
  }

  /**
   * Gets class path at the position calling the log function.
   * 
   * @return the current stack trace element
   */
  public static StackTraceElement getClassPath() {
    return classPath;
  }

  /**
   * Sets keep class path status.
   * 
   * @param keepClassPath the keeping status
   */
  public static void setKeepClassPath(boolean keepClassPath) {
    LoggerFormatDriver.keepClassPath = keepClassPath;
  }

  /**
   * The time function for formatting driver.
   * 
   * @return the current time in string
   */
  public String time() {
    LocalDateTime time = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    return time.format(formatter);
  }

  /**
   * The getType function for formatting driver.
   * 
   * @return the current type to log
   */
  public String getType() {
    switch (type) {
      case LOG:
        return "$fg(08)$bold()LOG    ";
      case WARNING:
        return "$fg(11)$bold()WARNING";
      case ERROR:
        return "$fg(01)$bold()ERROR  ";
      case INFO:
        return "$fg(10)$bold()INFO   ";
      case SQL:
        return "$fg(219)$bold()SQL    ";
      case ROUTE:
        return "$fg(15)$bold()ROUTE  ";
      default:
        return "$fg(242)$bold()UNKNOWN";
    }
  }

  /**
   * The type function for formatting driver.
   * 
   * @return the current logger level in string type
   * @throws FormatException if an error occurs
   */
  public String type() throws FormatException {
    Formatter formatter = new Formatter();
    return formatter.format(getType());
  }

  /**
   * The classPath function for formatting driver.
   * 
   * @return the current class path
   */
  public String classPath() {
    int logClassPath = Constants.getInteger("log.classpath");
    if (logClassPath > 0) {
      // Get the stacktrace
      StackTraceElement stackTrace;
      if (classPath != null) {
        stackTrace = classPath;
        if (keepClassPath) {
          keepClassPath = false;
        } else
          classPath = null;
      } else
        stackTrace = Thread.currentThread().getStackTrace()[8];

      // Build the classpath to log
      StringBuilder sb = new StringBuilder();
      if (logClassPath == 1 || logClassPath == 3) {
        sb.append(stackTrace.getFileName());
        sb.append(':');
        sb.append(stackTrace.getLineNumber());
      }
      if (logClassPath == 3)
        sb.append('(');
      if (logClassPath == 2 || logClassPath == 3) {
        sb.append(stackTrace.getClassName());
        sb.append('.');
        sb.append(stackTrace.getMethodName());
      }
      if (logClassPath == 3)
        sb.append(')');
      return sb.toString();
    } else
      return "";
  }

  /**
   * Generates string with specific number of space characters.
   * 
   * @param number the number of space characters to generate
   * @return the string of space characters
   */
  private String span(int number) {
    String res = "";

    for (int i = 0; i < number; i++)
      res += ' ';

    return res;
  }

  /**
   * The module function for formatting driver
   * 
   * @return the formatted module name
   * @throws FormatException if an error occurs while formatting
   */
  public String module() throws FormatException {
    // Get max module name length for nice styling
    int length = Loader.getMaxModuleNameLength();
    if (module != null) {
      StringBuilder sb = new StringBuilder();

      sb.append("$fg(15)[$fg(12)$bold()");
      sb.append(module);
      sb.append(span(length - module.length()));
      sb.append("$reset()$fg(15)] $reset()");

      Formatter formatter = new Formatter();
      return formatter.format(sb.toString());
    } else
      return "";
  }
}
