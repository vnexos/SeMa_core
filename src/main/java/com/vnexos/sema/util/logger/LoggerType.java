package com.vnexos.sema.util.logger;

/**
 * Enumerates all level of Logger.
 * 
 * @author Trần Việt Đăng Quang
 */
public enum LoggerType {
  NONE(null),
  UNKNOWN("UNKNOWN"),
  LOG("LOG"),
  INFO("INFO"),
  WARNING("WARNING"),
  ERROR("ERROR"),
  SQL("SQL"),
  ROUTE("ROUTE");

  private String value;

  private LoggerType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
