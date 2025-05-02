package com.vnexos.sema.database;

/**
 * Enumerates the supported database types in the system.
 * 
 * <p>
 * The supported database systems include:
 * <ul>
 * <li>{@link DatabaseType#UNKNOWN UNKNOWN} - For unsupported or unrecognized
 * databases
 * <li>{@link DatabaseType#MYSQL MYSQL} - MySQL database
 * <li>{@link DatabaseType#SQLSERVER SQLSERVER} - Microsoft SQL Server
 * <li>{@link DatabaseType#POSTGRESQL POSTGRESQL} - PostgreSQL database
 * <li>{@link DatabaseType#SQLITE SQLITE} - SQLite embedded database
 * <li>{@link DatabaseType#ORACLE ORACLE} - Oracle Database
 * <li>{@link DatabaseType#H2 H2} - H2 in-memory database
 * <li>{@link DatabaseType#DB2 DB2} - IBM DB2 database
 * <li>{@link DatabaseType#MARIADB MARIADB} - MariaDB database
 * </ul>
 * 
 * <p>
 * This enum provides a type-safe way to identify and work with different
 * database management systems throughout the application. Each database type is
 * associated with a numeric value for potential serialization or database
 * storage purposes.
 * 
 * @author Trần Việt Đăng Quang
 */
public enum DatabaseType {
  UNKNOWN(0),
  MYSQL(1),
  SQLSERVER(2),
  POSTGRESQL(3),
  SQLITE(4),
  ORACLE(5),
  H2(6),
  DB2(7),
  MARIADB(8);

  private int value;

  /**
   * Constructs a database type with its associated numeric value.
   * 
   * @param index the numeric identifier for this database type
   */
  private DatabaseType(int index) {
    this.value = index;
  }

  /**
   * Gets the numeric value associated with this database type.
   * 
   * @return the numeric identifier for this database type
   */
  public int getValue() {
    return value;
  }
}
