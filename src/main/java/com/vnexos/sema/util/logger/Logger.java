package com.vnexos.sema.util.logger;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import com.vnexos.sema.Constants;
import com.vnexos.sema.context.ServerContext;
import com.vnexos.sema.util.format.FormatException;
import com.vnexos.sema.util.format.Formatter;
import com.vnexos.sema.util.logger.system.SystemPrintStream;

/**
 * Implements all utilities for logging including logging to screen and logging
 * to file.
 * 
 * @author Trần Việt Đăng Quang
 */
public class Logger {
  private String prefix = "";
  private String postfix = "";
  private Formatter formatter;
  private FileLogger fileLogger;
  private PrintStream tmpOut;
  private PrintStream tmpErr;
  private boolean isClosed = false;
  private List<String> stringStream;

  /**
   * Formats the message.
   * 
   * @param message   the message to format
   * @param type      the type of logger
   * @param spanIndex the index of trace
   * @return the formatted message
   */
  private String getMessage(String message, LoggerType type, int spanIndex) {
    if (type == LoggerType.NONE) {
      return message;
    }

    // Message that include prefix and postfix
    String msg = prefix + message + postfix;

    // Logging level of message
    LoggerFormatDriver.setType(type);

    // If class path is not set, set to the new class path
    if (LoggerFormatDriver.getClassPath() == null) {
      StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[ServerContext.STACK_TRACE_INDEX
          + spanIndex];
      LoggerFormatDriver.setClassPath(stackTrace);
    }
    // Format the message
    try {
      msg = formatter.format(msg);
    } catch (FormatException e) {
    }
    return msg;
  }

  /**
   * Changes the default log of System.
   */
  private void initSystemLog() {
    try {
      // Store for avoid overlapped formatting
      tmpOut = System.out;
      tmpErr = System.err;

      // Set out print stream of System
      System.setOut(
          new SystemPrintStream(
              System.out,
              fileLogger,
              (msg, type, span) -> getMessage(msg, type, span),
              LoggerType.UNKNOWN));
      // Set error print stream of System
      System.setErr(
          new SystemPrintStream(
              System.err,
              fileLogger,
              (msg, type, span) -> getMessage(msg, type, span),
              LoggerType.ERROR));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  /**
   * Resets the log of system.
   */
  private void resetSystemLog() {
    System.setOut(tmpOut);
  }

  /**
   * Constructs a logger.
   * <p>
   * Private Logger to avoid creating an instance of Logger.
   */
  private Logger() {
    fileLogger = new FileLogger();
    formatter = new Formatter(LoggerFormatDriver.class);
    initSystemLog();
    stringStream = Arrays.asList(Constants.getString("log.level").split("\\|"));
  }

  /**
   * Sets prefix of message.
   * 
   * @param prefix the prefix to set
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Sets postfix of message.
   * 
   * @param postfix the postfix to set
   */
  public void setPostfix(String postfix) {
    this.postfix = postfix;
  }

  /**
   * Logs a message with type.
   * 
   * @param message the message to log
   * @param type    the logger level
   */
  private void log(String message, LoggerType type) {
    if (!isClosed &&
        (type == LoggerType.NONE || stringStream.stream().anyMatch(str -> str.contains(type.getValue())))) {
      resetSystemLog();
      String msg = getMessage(message, type, 0);
      System.out.println(msg);
      fileLogger.log(msg);
      LoggerFormatDriver.setModule(null);
      initSystemLog();
    }
  }

  /**
   * Logs a message at LOG level
   * 
   * @param message the message to log
   */
  public void log(String message) {
    log(message, LoggerType.LOG);
  }

  /**
   * Logs a message at INFO level
   * 
   * @param message the message to log
   */
  public void info(String message) {
    log(message, LoggerType.INFO);
  }

  /**
   * Logs a message at WARNING level
   * 
   * @param message the message to log
   */
  public void warning(String message) {
    log(message, LoggerType.WARNING);
  }

  /**
   * Logs a message at ERROR level
   * 
   * @param message the message to log
   */
  public void error(String message) {
    log(message, LoggerType.ERROR);
  }

  /**
   * Logs a message at SQL level
   * 
   * @param message the message to log
   */
  @SuppressWarnings("unused")
  private void sql(String message) {
    log(message, LoggerType.SQL);
  }

  /**
   * Logs a message at ROUTE level
   * 
   * @param message the message to log
   */
  @SuppressWarnings("unused")
  private void route(String message) {
    log(message, LoggerType.ROUTE);
  }

  /**
   * Builds a message for stack trace element
   * 
   * @param element the element of stack trace
   * @return the message
   */
  private String buildStackTrace(StackTraceElement element) {
    StringBuilder builder = new StringBuilder();
    builder.append("\tat ");
    builder.append(element.getClassName());
    builder.append('.');
    builder.append(element.getMethodName());
    builder.append(" [");
    builder.append(element.getFileName());
    builder.append(":");
    builder.append(element.getLineNumber());
    builder.append(']');
    return builder.toString();
  }

  /**
   * Recursive causes of throwable
   * 
   * @param throwable the throwable to log
   */
  private void logCause(Throwable throwable) {
    if (throwable == null)
      return;
    error("Caused by: " + throwable.getClass().getName() + ": " + throwable.getMessage());
    StackTraceElement[] causedElements = throwable.getStackTrace();
    for (int i = 0; i < 5; i++) {
      log(buildStackTrace(causedElements[i]), LoggerType.NONE);
    }
    if (causedElements.length >= 5)
      log("\t... " + (causedElements.length - 5) + " more ...", LoggerType.NONE);
  }

  /**
   * Logs exception
   * 
   * @param exception the exception to log
   */
  public void log(Exception exception) {
    // Class path will be reset after log, make sure it keep after log at the first
    // time
    LoggerFormatDriver.setKeepClassPath(true);
    // Log message of exception
    error(exception.getClass().getName() + ": " + exception.getMessage());

    // print all stack trace element of exception
    StackTraceElement[] elements = exception.getStackTrace();
    for (int i = 0; i < 20; i++) {
      if (i == elements.length)
        break;
      log(buildStackTrace(elements[i]), LoggerType.NONE);
    }

    // print the hidden stack trace element
    if (elements.length >= 20)
      log("\t... " + (elements.length - 20) + " more ...", LoggerType.NONE);

    // Print the cause
    Throwable throwable = exception.getCause();
    logCause(throwable);
  }

  /**
   * Closes logger
   */
  public void close() {
    System.setOut(tmpOut);
    System.setErr(tmpErr);
    fileLogger.close();
    isClosed = true;
  }
}
