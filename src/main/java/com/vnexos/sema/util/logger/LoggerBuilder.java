package com.vnexos.sema.util.logger;

import java.lang.reflect.InvocationTargetException;

import com.vnexos.sema.util.PrivateServiceConstructor;
import com.vnexos.sema.util.format.Formatter;
import com.vnexos.sema.util.format.FormatException;

/**
 * Handles building the logger.
 * 
 * @author Trần Việt Đăng Quang
 */
public class LoggerBuilder {
  private Logger logger;

  /**
   * Private constructor to avoid creating an instance of LoggerBuilder
   */
  private LoggerBuilder() {
  }

  /**
   * Initializes the logger builder.
   * 
   * @return the instance of LoggerBuilder itself
   */
  public static LoggerBuilder init() {
    LoggerBuilder lb = new LoggerBuilder();
    try {
      // Construct logger class
      lb.logger = PrivateServiceConstructor.createInstance(
          Logger.class,
          PrivateServiceConstructor.createClassTypes(),
          PrivateServiceConstructor.createObjects());

    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      System.out.println("There is something wrong with Logger, did you edit the source?");
      e.printStackTrace();
    }
    return lb;
  }

  /**
   * Sets the prefix of the log message.
   * 
   * @param prefix the prefix to set
   * @return the instance of LoggerBuilder itself
   * @throws FormatException if an error occurs while formatting
   */
  public LoggerBuilder setPrefix(String prefix) throws FormatException {
    Formatter formatter = new Formatter();
    logger.setPrefix(formatter.format(prefix));
    return this;
  }

  /**
   * Sets the postfix of the log message.
   * 
   * @param postfix the postfix to set
   * @return the instance of LoggerBuilder itself
   * @throws FormatException if an error occurs while formatting
   */
  public LoggerBuilder setPostfix(String postfix) throws FormatException {
    Formatter formatter = new Formatter();
    logger.setPostfix(formatter.format(postfix));
    return this;
  }

  /**
   * Completes build the Logger.
   * 
   * @return an instance of Logger
   */
  public Logger build() {
    return logger;
  }
}
