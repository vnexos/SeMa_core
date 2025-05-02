package com.vnexos.sema.database;

import java.sql.SQLException;
import com.vnexos.sema.Constants;
import com.vnexos.sema.database.annotations.Entity;

/**
 * Represents a database table by associated entity class
 * 
 * @author Trần Việt Đăng Quang
 * @see DatabaseType
 */
public class EntityTable {
  private Class<?> clazz;
  private String tableName;
  private DatabaseType type;

  /**
   * Constructs an entity as a database table
   * 
   * @param clazz  the entity class
   * @param entity the annotation to the entity class
   */
  public EntityTable(Class<?> clazz, Entity entity) {
    this.clazz = clazz;
    this.tableName = entity.tableName();
    this.type = Database.getType(Constants.getString("sql.url"));
  }

  /**
   * Gets an entity class associated with database table
   * 
   * @return the class of entity
   */
  public Class<?> getClazz() {
    return clazz;
  }

  /**
   * Gets the table name from entity class annotation
   * 
   * @return the entity table
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Gets the type of the database engine-specific
   * 
   * @return the type of the database
   */
  public DatabaseType getType() {
    return type;
  }

  /**
   * Generates CREATE TABLE statement for the entity class
   * 
   * @param engine the specified database engine
   * @return the generated CREATE TABLE SQL statement
   * @throws SQLException if the SQL statement cannot be generated
   */
  public String generateCreateTableSql(DatabaseEngine engine) throws SQLException {
    return engine.generateCreateTable(this.clazz);
  }
}
