/*
 * Copyright (c) 2025, VNExos and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package com.vnexos.sema.context;

import java.nio.file.Path;

import com.vnexos.sema.util.logger.Logger;
import com.vnexos.sema.util.logger.LoggerFormatDriver;

/**
 * Provides server context information and logging functionality.
 * 
 * <p>
 * This class maintains server-specific configuration (like file paths) and
 * provides methods for logging messages at different severity levels.
 * All log entries are automatically annotated with stack trace information.
 * 
 * @author Trần Việt Đăng Quang
 * @see Logger
 */
public class ServerContext {
  /**
   * The server's base file system path.
   */
  private String path;

  /**
   * The logger instance used for all logging operations.
   */
  private Logger logger;

  /**
   * The index of the stack trace that point to the caller.
   */
  public static final int STACK_TRACE_INDEX = 3;

  /**
   * Private constructor to prevent direct instantiation.
   * <p>
   * This forces creation through proper initialization channels.
   */
  @SuppressWarnings("unused")
  private ServerContext() {
  }

  /**
   * Copy construct that creates a new context based on an existing one.
   * 
   * @param context the source context to copy properties from
   */
  protected ServerContext(ServerContext context) {
    path = context.path;
    logger = context.logger;
  }

  /**
   * Gets the server's base file system path.
   * 
   * @return the current server path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the server's base file system path.
   * 
   * @param path the new path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Sets the logger instance for this context.
   * 
   * @param logger the logger instance to use
   */
  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  /**
   * Captures and sets the current stack trace information for logging.
   * <p>
   * Uses {@link #STACK_TRACE_INDEX} to skip internal stack frames.
   */
  public void setStackTrace() {
    StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[STACK_TRACE_INDEX];
    LoggerFormatDriver.setClassPath(stackTrace);
  }

  /**
   * Logs a message at the default log level.
   * 
   * @param str the message to log
   */
  public void log(String str) {
    setStackTrace();
    logger.log(str);
  }

  /**
   * Logs an exception with its stack trace.
   * 
   * @param exception the exception to log
   */
  public void log(Exception exception) {
    setStackTrace();
    logger.log(exception);
  }

  /**
   * Logs an informational message.
   * 
   * @param str the informational message
   */
  public void info(String str) {
    setStackTrace();
    logger.info(str);
  }

  /**
   * Logs a warning message.
   * 
   * @param str the warning message
   */
  public void warning(String str) {
    setStackTrace();
    logger.warning(str);
  }

  /**
   * Logs an error message.
   * 
   * @param str the error message
   */
  public void error(String str) {
    setStackTrace();
    logger.error(str);
  }

  /**
   * Joins the server's base path with additional path components.
   * 
   * @param path variable number of path components to join
   * @return the combined absolute path
   */
  public String joinPath(String... path) {
    return Path.of(this.path, path).toString();
  }

  /**
   * Gets the current logger instance.
   * 
   * @return the logger instance.
   */
  public Logger getLogger() {
    return logger;
  }
}
