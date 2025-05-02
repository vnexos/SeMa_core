package com.vnexos.sema.database;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vnexos.sema.Constants;
import com.vnexos.sema.database.annotations.Column;
import com.vnexos.sema.database.annotations.Entity;
import com.vnexos.sema.database.engine.DB2Engine;
import com.vnexos.sema.database.engine.H2Engine;
import com.vnexos.sema.database.engine.MariaDBEngine;
import com.vnexos.sema.database.engine.MySqlEngine;
import com.vnexos.sema.database.engine.OracleEngine;
import com.vnexos.sema.database.engine.PostgreSqlEngine;
import com.vnexos.sema.database.engine.SQLiteEngine;
import com.vnexos.sema.database.engine.SqlServerEngine;
import com.vnexos.sema.loader.json.LocalDateAdapter;
import com.vnexos.sema.loader.json.LocalDateTimeAdapter;
import com.vnexos.sema.loader.json.LocalTimeAdapter;
import com.vnexos.sema.util.ClassUtils;
import com.vnexos.sema.util.PrivateServiceConstructor;
import com.vnexos.sema.util.logger.Logger;
import com.vnexos.sema.util.logger.LoggerFormatDriver;

/**
 * Main database access class that manages database connections, table creation,
 * and provides database-specific functionality.
 * 
 * <p>
 * This class serves as the central point for database operations, including:
 * <ul>
 * <li>Connection pool management</li>
 * <li>Database engine detection and initialization</li>
 * <li>Automatic table creation based on entity annotations</li>
 * <li>Database type-specific SQL generation</li>
 * </ul>
 * 
 * <p>
 * The class uses a {@link ConnectionPool} for connection management and
 * supports
 * multiple database engines through the {@link DatabaseEngine} interface.
 * 
 * @author Trần Việt Đăng Quang
 * @see ConnectionPool
 * @see DatabaseEngine
 * @see Entity
 * @see Column
 */
public class Database {
  private static ConnectionPool pool;
  private static DatabaseEngine engine;
  private static List<String> createdTable;
  private static Queue<EntityTable> queue;
  static {
    createdTable = new ArrayList<>();
    queue = new LinkedList<>();
  }

  /**
   * Avoids constructing the class
   */
  private Database() {
  }

  /**
   * Pre-configured Gson instance with custom type adapters for Java time classes.
   * Supports serialization/deserialization of:
   * <ul>
   * <li>{@link LocalDateTime}</li>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalTime}</li>
   * </ul>
   */
  public static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
      .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
      .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
      .create();

  /**
   * Logs an SQL command using the configured logger.
   * Falls back to simple console output if reflection-based logging fails.
   * 
   * @param command the SQL command to log
   */
  private static void log(String command) {
    try {
      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
      LoggerFormatDriver.setClassPath(elements[2]);
      PrivateServiceConstructor.invokeFunction(
          Logger.class, "sql", Constants.context.getLogger(),
          PrivateServiceConstructor.createClassTypes(String.class),
          PrivateServiceConstructor.createObjects(command));
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
      Constants.context.log("SQL: " + command);
    }
  }

  /**
   * Determines the database type from the JDBC URL.
   * 
   * @param url the JDBC connection URL
   * @return the detected {@link DatabaseType}, or UNKNOWN if not recognized
   */
  public static DatabaseType getType(String url) {
    if (url.startsWith("jdbc:mysql://"))
      return DatabaseType.MYSQL;
    else if (url.startsWith("jdbc:sqlserver://"))
      return DatabaseType.SQLSERVER;
    else if (url.startsWith("jdbc:postgresql://")) {
      return DatabaseType.POSTGRESQL;
    } else if (url.startsWith("jdbc:sqlite:")) {
      return DatabaseType.SQLITE;
    } else if (url.startsWith("jdbc:oracle:thin:@//")) {
      return DatabaseType.ORACLE;
    } else if (url.startsWith("jdbc:h2:")) {
      return DatabaseType.H2;
    } else if (url.startsWith("jdbc:db2://")) {
      return DatabaseType.DB2;
    } else if (url.startsWith("jdbc:mariadb://")) {
      return DatabaseType.MARIADB;
    }
    return DatabaseType.UNKNOWN;
  }

  /**
   * Extracts the base URL without database name for initial connection.
   * 
   * @param url  the full JDBC URL
   * @param type the database type
   * @return the base URL without database specification
   */
  private static String getBaseUrlWithoutDatabase(String url, DatabaseType type) {
    if (type == DatabaseType.MYSQL) {
      int lastSlash = url.lastIndexOf('/');
      if (lastSlash != -1 && lastSlash > "jdbc:mysql://".length()) {
        return url.substring(0, lastSlash);
      }
      return url;
    } else {
      int dbIndex = url.indexOf(";database=");
      if (dbIndex != -1) {
        return url.substring(0, dbIndex);
      }
      return url;
    }
  }

  /**
   * Initializes the database connection pool and selects the appropriate
   * database engine based on configuration.
   * 
   * @throws SQLException if driver loading or initial connection fails
   */
  @SuppressWarnings("unused")
  private static void init() throws SQLException {
    try {
      Class.forName(Constants.getString("sql.driver"));
    } catch (ClassNotFoundException e) {
      throw new SQLException("Cannot find Driver for database.", e);
    }

    String baseUrl = Constants.getString("sql.url");
    String dbName = Constants.getString("sql.db");
    DatabaseType type = getType(baseUrl);
    switch (type) {
      case MYSQL:
        engine = new MySqlEngine();
        break;
      case SQLSERVER:
        engine = new SqlServerEngine();
        break;
      case POSTGRESQL:
        engine = new PostgreSqlEngine();
        break;
      case SQLITE:
        engine = new SQLiteEngine();
        break;
      case ORACLE:
        engine = new OracleEngine();
        break;
      case H2:
        engine = new H2Engine();
        break;
      case DB2:
        engine = new DB2Engine();
        break;
      case MARIADB:
        engine = new MariaDBEngine();
        break;
      default:
        break;
    }

    Properties props = new Properties();
    props.setProperty("user", Constants.getString("sql.user"));
    props.setProperty("password", Constants.getString("sql.pass"));

    if (Constants.getBoolean("sql.setup-on-start")) {
      String checkUrl = getBaseUrlWithoutDatabase(baseUrl, type);
      try (Connection conn = DriverManager.getConnection(checkUrl, props);
          Statement stmt = conn.createStatement()) {

        String createDbSql = engine.generateCreateDatabase(dbName);
        log(createDbSql);
        stmt.executeUpdate(createDbSql);
      } catch (SQLException e) {
        Constants.context.log(e);
        return;
      }
    }

    String fullUrl;
    if (type == DatabaseType.MYSQL) {
      fullUrl = String.format("%s/%s", baseUrl, dbName);
    } else {
      fullUrl = String.format("%s;database=%s", baseUrl, dbName);
    }

    try {
      pool = new ConnectionPool(fullUrl, props, Constants.getInteger("sql.maxPoolSize"));
    } catch (SQLException e) {
      Constants.context.log(e);
    }
  }

  /**
   * Processes table creation for an entity, handling foreign key dependencies.
   * 
   * @param stm         the statement to execute SQL
   * @param entityTable the entity table definition to process
   * @throws SQLException if table creation fails
   */
  private static void processCreateTable(Statement stm, EntityTable entityTable) throws SQLException {
    if (createdTable.contains(entityTable.getTableName()))
      return;

    List<Field> fields = ClassUtils.getAllFields(entityTable.getClazz());

    for (Field field : fields) {
      Column column = field.getAnnotation(Column.class);
      if (column == null || column.foreignKey() == Object.class)
        continue;

      Class<?> entityClass = column.foreignKey();
      Entity entity = entityClass.getAnnotation(Entity.class);
      if (entity == null)
        continue;

      if (!createdTable.contains(entity.tableName())) {
        queue.add(entityTable);
        return;
      }
    }
    String sql = entityTable.generateCreateTableSql(engine);
    log(sql);
    stm.execute(sql);
    createdTable.add(entityTable.getTableName());
  }

  /**
   * Loads the database schema by creating tables for all specified entities.
   * Handles foreign key dependencies by processing tables in correct order.
   * 
   * @param entityTables list of entity table definitions to create
   * @throws SQLException if table creation fails
   */
  public static void loadDatabase(List<EntityTable> entityTables) throws SQLException {
    Connection conn = getConnection();
    if (Constants.getBoolean("sql.setup-on-start")) {
      Statement stm = conn.createStatement();
      for (EntityTable entityTable : entityTables) {
        processCreateTable(stm, entityTable);
      }

      while (!queue.isEmpty()) {
        processCreateTable(stm, queue.poll());
      }
    }
  }

  /**
   * Shuts down the connection pool, closing all active connections.
   */
  public static void shutdown() {
    pool.shutdown();
  }

  /**
   * Gets a database connection from the pool.
   * 
   * @return a usable database connection
   * @throws SQLException if no connection is available
   */
  public static Connection getConnection() throws SQLException {
    return pool.getConnection();
  }

  /**
   * Gets the database engine implementation for the current database type.
   * 
   * @return the configured database engine
   */
  public static DatabaseEngine getEngine() {
    return engine;
  }
}