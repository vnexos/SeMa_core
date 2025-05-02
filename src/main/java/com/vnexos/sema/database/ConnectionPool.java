package com.vnexos.sema.database;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * A connection pool implementation that manages a pool of database connections
 * to improve performance and resource utilization.
 * 
 * <p>
 * This class implements the {@code DataSource} interface and maintains a pool
 * of active database connections. When a connection is requested, it either
 * returns an available connection from the pool or creates a new one if the
 * pool hasn't reached its maximum size. Connections are automatically returned
 * to the pool when closed by the client.
 * 
 * <p>
 * The pool uses a {@code BlockingQueue} to manage available connections and
 * handles connection validation, timeout, and cleanup operations.
 * 
 * @author Trần Việt Đăng Quang
 */
public class ConnectionPool implements DataSource {
  private final BlockingQueue<Connection> pool;
  private final String jdbcUrl;
  private final Properties props;
  private final int maxSize;
  private volatile int currentSize = 0;
  private PrintWriter logWriter = new PrintWriter(System.out);
  private int loginTimeout = 30;

  /**
   * Creates a new connection pool with the specified JDBC URL, connection
   * properties, and maximum pool size.
   * 
   * @param jdbcUrl the JDBC URL for database connections
   * @param props   the connection properties (username, password, etc.)
   * @param maxSize the maximum number of connections in the pool
   * @throws SQLException if initial connection setup fails
   */
  public ConnectionPool(String jdbcUrl, Properties props, int maxSize) throws SQLException {
    this.jdbcUrl = jdbcUrl;
    this.props = props;
    this.maxSize = maxSize;
    this.pool = new LinkedBlockingQueue<>(maxSize);
  }

  /**
   * Gets a connection from the pool. If no connections are available and the pool
   * hasn't reached maximum size, creates a new connection. Otherwise waits for
   * the configured timeout period for a connection to become available.
   * 
   * @return a usable database connection
   * @throws SQLException if no connection is available within timeout or if
   *                      connection validation fails
   */
  @Override
  public Connection getConnection() throws SQLException {
    try {
      Connection conn = pool.poll();
      if (conn != null && isValid(conn)) {
        return wrap(conn);
      }

      synchronized (this) {
        if (currentSize < maxSize) {
          Connection newConn = DriverManager.getConnection(jdbcUrl, props);
          currentSize++;
          return wrap(newConn);
        }
      }

      // Wait for an available connection
      conn = pool.poll(loginTimeout, TimeUnit.SECONDS);
      if (conn == null) {
        throw new SQLException("Timeout: No available connection in the pool");
      }

      if (isValid(conn)) {
        return wrap(conn);
      } else {
        closeSilently(conn);
        return getConnection(); // Try again recursively
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SQLException("Interrupted while waiting for a connection", e);
    }
  }

  /**
   * Gets a connection using specific credentials, bypassing the connection pool.
   * This creates a new connection each time and doesn't pool it.
   * 
   * @param username the database username
   * @param password the database password
   * @return a new database connection
   * @throws SQLException if connection creation fails
   */
  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    Properties overrideProps = new Properties(props);
    overrideProps.put("user", username);
    overrideProps.put("password", password);
    return DriverManager.getConnection(jdbcUrl, overrideProps); // Bypasses pool
  }

  /**
   * Validates that a connection is still open and usable.
   * 
   * @param conn the connection to validate
   * @return true if the connection is valid, false otherwise
   */
  private boolean isValid(Connection conn) {
    try {
      return conn != null && !conn.isClosed() && conn.isValid(2);
    } catch (SQLException e) {
      return false;
    }
  }

  /**
   * Closes a connection silently, ignoring any exceptions.
   * 
   * @param conn the connection to close
   */
  private void closeSilently(Connection conn) {
    try {
      if (conn != null)
        conn.close();
    } catch (SQLException ignored) {
      // Ignore
    }
  }

  /**
   * Wraps a real connection in a PooledConnection proxy that returns itself
   * to the pool when closed.
   * 
   * @param realConn the actual database connection to wrap
   * @return a pooled connection wrapper
   */
  private Connection wrap(Connection realConn) {
    return new PooledConnection(realConn, this);
  }

  /**
   * Returns a connection to the pool. Called by PooledConnection when closed.
   * If the connection is no longer valid, it is discarded and pool size is
   * decremented.
   * 
   * @param conn the connection to release back to the pool
   */
  protected void release(Connection conn) {
    try {
      if (isValid(conn)) {
        if (!pool.offer(conn)) {
          synchronized (this) {
            currentSize--;
          }
          closeSilently(conn);
        }
      } else {
        synchronized (this) {
          currentSize--;
        }
        closeSilently(conn);
      }
    } catch (Exception e) {
      synchronized (this) {
        currentSize--;
      }
      closeSilently(conn);
    }
  }

  /**
   * Shuts down the connection pool by closing all active connections and
   * clearing the pool.
   */
  public void shutdown() {
    for (Connection conn : pool) {
      closeSilently(conn);
    }
    pool.clear();
    currentSize = 0;
  }

  @Override
  public PrintWriter getLogWriter() {
    return logWriter;
  }

  @Override
  public void setLogWriter(PrintWriter out) {
    this.logWriter = out;
  }

  @Override
  public void setLoginTimeout(int seconds) {
    this.loginTimeout = seconds;
  }

  @Override
  public int getLoginTimeout() {
    return loginTimeout;
  }

  @Override
  public Logger getParentLogger() {
    return Logger.getGlobal();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this))
      return iface.cast(this);
    throw new SQLException("Not a wrapper for " + iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return iface.isInstance(this);
  }

  // Inner class for pooled connection proxy
  private static class PooledConnection implements Connection {
    private final Connection real;
    private final ConnectionPool pool;
    private boolean closed = false;

    PooledConnection(Connection real, ConnectionPool pool) {
      this.real = real;
      this.pool = pool;
    }

    @Override
    public void close() throws SQLException {
      if (!closed) {
        closed = true;
        pool.release(real);
      }
    }

    @Override
    public boolean isClosed() throws SQLException {
      return closed || real.isClosed();
    }

    // Delegate everything else
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return real.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return real.isWrapperFor(iface);
    }

    @Override
    public Statement createStatement() throws SQLException {
      return real.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
      return real.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
      return real.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
      return real.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
      real.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
      return real.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
      real.commit();
    }

    @Override
    public void rollback() throws SQLException {
      real.rollback();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
      return real.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
      real.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
      return real.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
      real.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
      return real.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
      real.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
      return real.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
      return real.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
      real.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
      return real.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException {
      return real.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return real.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
      real.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
      return real.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
      return real.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
      return real.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
      real.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
      real.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
      return real.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {
      return real.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {
      return real.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
      return real.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
      return real.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
      return real.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
      return real.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
      return real.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
      return real.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
      return real.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
      return real.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
      real.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
      real.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
      return real.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
      return real.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
      return real.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
      return real.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
      real.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
      return real.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
      real.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
      real.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
      return real.getNetworkTimeout();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
      return real.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
      real.setTypeMap(map);
    }
  }
}
