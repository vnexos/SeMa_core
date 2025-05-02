package com.vnexos.sema.database;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Defines the interface for database engine-specific implements
 * 
 * <p>
 * This interface provides methods for generating database-specific SQL
 * statements and handling database-specific syntax requirements.
 * Implementations should provide functionality tailored to specific database
 * systems (MySQL, SQLServer, PostgreSQL, etc.)
 * 
 * <p>
 * The interface covers:
 * <ul>
 * <li>Database object naming conventions (quoting identifiers)
 * <li>SQL statement generation (CREATE, INSERT, SELECT, UPDATE, DELETE)
 * </ul>
 * 
 * <p>
 * The engine can only and should generate SQL statement. DO NOT use to execute
 * an SQL statement.
 * 
 * @author Trần Việt Đăng Quang
 * @see com.vnexos.sema.database.annotations.Column
 */
public interface DatabaseEngine {
  /**
   * Gets the opening character(s) for quoted database object names (tables,
   * columns)
   * 
   * @return the opening quote character(s) (e.g., "[" for SQL Server, "`" for
   *         MySQL)
   */
  public String getNameOpening();

  /**
   * Gets the ending character(s) for quoted database object names (tables,
   * columns)
   * 
   * @return the ending quote character(s) (e.g., "[" for SQL Server, "`" for
   *         MySQL)
   */
  public String getNameEnding();

  /**
   * Gets the opening character(s) for quoted values in SQL statements.
   * 
   * @return the opening quote character(s) (mostly be "'")
   */
  public String getValueOpening();

  /**
   * Gets the ending character(s) for quoted values in SQL statements.
   * 
   * @return the ending quote character(s) (mostly be "'")
   */
  public String getValueEnding();

  /**
   * Gets the database type which engine implementation supports.
   * 
   * @return the database type constant
   */
  public DatabaseType getType();

  /**
   * Maps a Java field to its corresponding database column type.
   * 
   * @param field the Java field to map
   * @return the database-specific column type definition
   * @throws SQLException if the field cannot be mapped to a database type
   */
  public String getColumnType(Field field) throws SQLException;

  /**
   * Generates a CREATE DATABASE statement for the specified database name.
   * 
   * @param databaseName the name of the database to create
   * @return the complete CREATE DATABASE SQL statement
   * @throws SQLException if the statement cannot be generated
   */
  public String generateCreateDatabase(String databaseName) throws SQLException;

  /**
   * Generates a CREATE DATABASE statement for the specified entity class.
   * 
   * @param clazz the entity class to create a table for
   * @return the complete CREATE TABLE SQL statement
   * @throws SQLException if the statement cannot be generated
   */
  public String generateCreateTable(Class<?> clazz) throws SQLException;

  /**
   * Generates an INSERT statement for the specified table and data object.
   * 
   * @param table the target table name
   * @param data  the object containing data to insert
   * @return the complete INSERT SQL statement
   * @throws SQLException if the statement cannot be generated
   */
  public String generateInsert(String table, Object data) throws SQLException;

  /**
   * Generates a SELECT statement for the specified table, columns, and condition.
   * 
   * @param tableName the table to query
   * @param columns   the columns to select (empty array for all columns)
   * @param condition the condition (must including WHERE keyword)
   * @return the complete SELECT SQL statement
   * @throws SQLException if the statement cannot be generated
   */
  public String generateGet(String tableName, String[] columns, String condition) throws SQLException;

  /**
   * Generates a COUNT statement for the specified table and condition.
   * 
   * @param tableName the table to count rows in
   * @param condition the condition (must including WHERE keyword)
   * @return the complete COUNT SQL statement
   * @throws SQLException if the statement cannot be generated
   */
  public String generateCount(String tableName, String condition) throws SQLException;

  /**
   * Generates an UPDATE statement for the specified table, data, and condition.
   * 
   * @param tableName the table to update
   * @param data      the object containing data to update
   * @param where     the WHERE condition (without the WHERE keyword)
   * @return the complete UPDATE SQL statement
   * @throws SQLException if the statement cannot be generated
   */
  public String generateUpdate(String tableName, Object data, String where) throws SQLException;

  /**
   * Generates an DELETE statement for the specified table and condition.
   * 
   * @param tableName the table to delete from
   * @param where     the WHERE condition (without the WHERE keyword)
   * @return the complete DELETE SQL statement
   * @throws SQLException if the statement cannot be generate
   */
  public String generateDelete(String tableName, String where) throws SQLException;
}
