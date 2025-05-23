package com.vnexos.sema.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class will wrap the Driver loaded from URLClassLoader because the
 * DriverWrapper was trusted by the system class loader.
 * 
 * @author Trần Việt Đăng Quang
 */
public class DriverWrapper implements Driver {
  private final Driver driver;

  /**
   * Constructs by the driver
   * 
   * @param d driver to copy
   */
  public DriverWrapper(Driver d) {
    this.driver = d;
  }

  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    return driver.connect(url, info);
  }

  @Override
  public boolean acceptsURL(String url) throws SQLException {
    return driver.acceptsURL(url);
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
    return driver.getPropertyInfo(url, info);
  }

  @Override
  public int getMajorVersion() {
    return driver.getMajorVersion();
  }

  @Override
  public int getMinorVersion() {
    return driver.getMinorVersion();
  }

  @Override
  public boolean jdbcCompliant() {
    return driver.jdbcCompliant();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return driver.getParentLogger();
  }
}