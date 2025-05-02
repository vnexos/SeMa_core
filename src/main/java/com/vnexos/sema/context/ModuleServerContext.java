/*
 * Copyright (c) 2025, VNExos and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package com.vnexos.sema.context;

import com.vnexos.sema.loader.Module;
import com.vnexos.sema.util.logger.LoggerFormatDriver;

/**
 * A specialized {@link ServerContext com.vnexos.sema.context.ServerContext}
 * that associates logging with a specific module.
 * 
 * <p>
 * This context ensures that all log entries will be tagged with the module
 * name, making it easier to trace log message back to their source module.
 * 
 * <p>
 * The class extends {@code ServerContext} and overrides all loggin methods
 * to include information before delegating to the parent logger.
 * 
 * @author Trần Việt Đăng Quang
 * @see ServerContext
 * @see Module
 * @see LoggerFormatDriver
 */
public class ModuleServerContext extends ServerContext {
  private Module module;

  /**
   * Creates a new ModuleServerContext with the with the specified parent context
   * and module
   * 
   * @param context the parent server context to inherit configuration from
   * @param module  the module to associate with this context
   */
  private ModuleServerContext(ServerContext context, Module module) {
    super(context);
    this.module = module;
  }

  /**
   * Returns the module associated with this contex
   * 
   * @return the module instance
   */
  public Module getModule() {
    return module;
  }

  /**
   * Logs an exception with module information
   * 
   * @param exception the exception to log
   */
  @Override
  public void log(Exception exception) {
    setStackTrace();
    LoggerFormatDriver.setModule(module.getModuleName());
    getLogger().log(exception);
  }

  /**
   * Logs an message with module information at the default log level.
   * 
   * @param str the message to log
   */
  @Override
  public void log(String str) {
    setStackTrace();
    LoggerFormatDriver.setModule(module.getModuleName());
    getLogger().log(str);
  }

  /**
   * Logs an informational message with module information
   * 
   * @param str the informational message
   */
  @Override
  public void info(String str) {
    setStackTrace();
    LoggerFormatDriver.setModule(module.getModuleName());
    getLogger().info(str);
  }

  /**
   * Logs a warning message with module information
   * 
   * @param str the warning message
   */
  @Override
  public void warning(String str) {
    setStackTrace();
    LoggerFormatDriver.setModule(module.getModuleName());
    getLogger().warning(str);
  }

  /**
   * Logs an error message with module information
   * 
   * @param str the error message
   */
  @Override
  public void error(String str) {
    setStackTrace();
    LoggerFormatDriver.setModule(module.getModuleName());
    getLogger().error(str);
  }
}
