package com.vnexos.sema.database.engine;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vnexos.sema.database.Database;
import com.vnexos.sema.database.DatabaseEngine;
import com.vnexos.sema.database.DatabaseType;
import com.vnexos.sema.database.IdType;
import com.vnexos.sema.database.annotations.Column;
import com.vnexos.sema.database.annotations.Entity;
import com.vnexos.sema.database.annotations.Identity;
import com.vnexos.sema.util.ClassUtils;
import com.vnexos.sema.util.StringUtils;

/**
 * Provides engine for MariaDB database
 * 
 * @author Trần Việt Đăng Quang
 * @see DatabaseEngine
 */
public class MariaDBEngine implements DatabaseEngine {

  private enum ColumnType {
    TINYINT("TINYINT"),
    SMALLINT("SMALLINT"),
    INTEGER("INTEGER"),
    BIGINT("BIGINT"),
    DECIMAL("DECIMAL(19,4)"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    CHAR("CHAR(%d)"),
    VARCHAR("VARCHAR(%d)"),
    TEXT("TEXT"),
    BINARY("BINARY(%d)"),
    BLOB("BLOB"),
    DATE("DATE"),
    TIME("TIME"),
    DATETIME("DATETIME"),
    TIMESTAMP("TIMESTAMP"),
    BOOLEAN("BOOLEAN"),
    UUID("CHAR(36)"); // MariaDB does not have a native UUID type

    private final String type;

    ColumnType(String type) {
      this.type = type;
    }

    String getType(int length) {
      return type.contains("%d") ? String.format(type, length) : type;
    }
  }

  private static final Map<Class<?>, TypeMapping> TYPE_MAPPINGS = Map.ofEntries(
      Map.entry(boolean.class, new TypeMapping(ColumnType.BOOLEAN, 0)),
      Map.entry(Boolean.class, new TypeMapping(ColumnType.BOOLEAN, 0)),
      Map.entry(byte.class, new TypeMapping(ColumnType.TINYINT, 0)),
      Map.entry(Byte.class, new TypeMapping(ColumnType.TINYINT, 0)),
      Map.entry(short.class, new TypeMapping(ColumnType.SMALLINT, 0)),
      Map.entry(Short.class, new TypeMapping(ColumnType.SMALLINT, 0)),
      Map.entry(int.class, new TypeMapping(ColumnType.INTEGER, 0)),
      Map.entry(Integer.class, new TypeMapping(ColumnType.INTEGER, 0)),
      Map.entry(long.class, new TypeMapping(ColumnType.BIGINT, 0)),
      Map.entry(Long.class, new TypeMapping(ColumnType.BIGINT, 0)),
      Map.entry(java.math.BigInteger.class, new TypeMapping(ColumnType.BIGINT, 0)),
      Map.entry(float.class, new TypeMapping(ColumnType.FLOAT, 0)),
      Map.entry(Float.class, new TypeMapping(ColumnType.FLOAT, 0)),
      Map.entry(double.class, new TypeMapping(ColumnType.DOUBLE, 0)),
      Map.entry(Double.class, new TypeMapping(ColumnType.DOUBLE, 0)),
      Map.entry(char.class, new TypeMapping(ColumnType.CHAR, 1)),
      Map.entry(Character.class, new TypeMapping(ColumnType.CHAR, 1)),
      Map.entry(String.class, new TypeMapping(ColumnType.VARCHAR, 255)),
      Map.entry(UUID.class, new TypeMapping(ColumnType.UUID, 0)),
      Map.entry(java.util.Date.class, new TypeMapping(ColumnType.DATE, 0)),
      Map.entry(java.sql.Date.class, new TypeMapping(ColumnType.DATE, 0)),
      Map.entry(java.time.LocalDate.class, new TypeMapping(ColumnType.DATE, 0)),
      Map.entry(java.sql.Time.class, new TypeMapping(ColumnType.TIME, 0)),
      Map.entry(java.time.LocalTime.class, new TypeMapping(ColumnType.TIME, 0)),
      Map.entry(java.sql.Timestamp.class, new TypeMapping(ColumnType.DATETIME, 0)),
      Map.entry(java.time.LocalDateTime.class, new TypeMapping(ColumnType.DATETIME, 0)),
      Map.entry(java.time.Instant.class, new TypeMapping(ColumnType.DATETIME, 0)),
      Map.entry(byte[].class, new TypeMapping(ColumnType.BLOB, 0)),
      Map.entry(Byte[].class, new TypeMapping(ColumnType.BLOB, 0)),
      Map.entry(java.math.BigDecimal.class, new TypeMapping(ColumnType.DECIMAL, 0)));

  private static class TypeMapping {
    final ColumnType columnType;
    final int length;

    TypeMapping(ColumnType columnType, int length) {
      this.columnType = columnType;
      this.length = length;
    }
  }

  @Override
  public String getNameOpening() {
    return "`";
  }

  @Override
  public String getNameEnding() {
    return "`";
  }

  @Override
  public String getValueOpening() {
    return "'";
  }

  @Override
  public String getValueEnding() {
    return "'";
  }

  @Override
  public DatabaseType getType() {
    return DatabaseType.MARIADB;
  }

  @Override
  public String getColumnType(Field field) throws SQLException {
    Column column = field.getAnnotation(Column.class);
    String customType = (column != null && !column.type().isEmpty()) ? column.type().toUpperCase() : "";
    int length = column != null ? column.length() : 255;

    if (!customType.isEmpty()) {
      return getCustomColumnType(customType, length);
    }
    return getTypeBasedColumnType(field.getType());
  }

  private String getCustomColumnType(String customType, int length) {
    switch (customType) {
      case "TINYINT":
        return ColumnType.TINYINT.getType(length);
      case "SMALLINT":
        return ColumnType.SMALLINT.getType(length);
      case "INTEGER":
      case "INT":
        return ColumnType.INTEGER.getType(length);
      case "BIGINT":
        return ColumnType.BIGINT.getType(length);
      case "DECIMAL":
      case "NUMERIC":
        return ColumnType.DECIMAL.getType(length);
      case "FLOAT":
        return ColumnType.FLOAT.getType(length);
      case "DOUBLE":
        return ColumnType.DOUBLE.getType(length);
      case "CHAR":
        return ColumnType.CHAR.getType(length);
      case "VARCHAR":
        return ColumnType.VARCHAR.getType(length);
      case "TEXT":
        return ColumnType.TEXT.getType(length);
      case "BINARY":
        return ColumnType.BINARY.getType(length);
      case "BLOB":
        return ColumnType.BLOB.getType(length);
      case "DATE":
        return ColumnType.DATE.getType(length);
      case "TIME":
        return ColumnType.TIME.getType(length);
      case "DATETIME":
        return ColumnType.DATETIME.getType(length);
      case "TIMESTAMP":
        return ColumnType.TIMESTAMP.getType(length);
      case "BOOLEAN":
        return ColumnType.BOOLEAN.getType(length);
      case "UUID":
      case "UNIQUEIDENTIFIER":
        return ColumnType.UUID.getType(length);
      default:
        return customType;
    }
  }

  private String getTypeBasedColumnType(Class<?> type) {
    if (type.isEnum()) {
      return ColumnType.VARCHAR.getType(255);
    }

    TypeMapping mapping = TYPE_MAPPINGS.get(type);
    if (mapping != null) {
      return mapping.columnType.getType(mapping.length);
    }

    throw new IllegalArgumentException("Unsupported field type: " + type.getName());
  }

  @Override
  public String generateCreateDatabase(String databaseName) throws SQLException {
    return "CREATE DATABASE IF NOT EXISTS " + getNameOpening() + databaseName + getNameEnding();
  }

  @Override
  public String generateCreateTable(Class<?> clazz) throws SQLException {
    Entity entity = clazz.getAnnotation(Entity.class);
    if (entity == null)
      throw new SQLException("Class must be annotated with @Entity");

    StringBuilder sql = new StringBuilder();
    StringBuilder stored = new StringBuilder();

    sql.append("CREATE TABLE IF NOT EXISTS ")
        .append(getNameOpening())
        .append(StringUtils.convertCamelToSnake(entity.tableName()))
        .append(getNameEnding())
        .append(" (\n");

    List<Field> fields = ClassUtils.getAllFields(clazz);
    boolean firstField = true;

    for (Field field : fields) {
      Identity identity = field.getAnnotation(Identity.class);
      if (identity != null) {
        sql.append(generateFieldSql(field)).append(",\n");
      } else {
        if (!firstField)
          stored.append(",\n");
        firstField = false;
        stored.append(generateFieldSql(field));
      }
    }
    sql.append(stored);

    sql.append("\n) ENGINE=InnoDB");
    return sql.toString();
  }

  @Override
  public String generateInsert(String table, Object data) throws SQLException {
    if (table == null || table.trim().isEmpty())
      throw new SQLException("Table name cannot be null or empty");
    if (data == null)
      throw new SQLException("Data object cannot be null");

    JsonObject object = Database.gson.toJsonTree(data).getAsJsonObject();
    StringBuilder sql = new StringBuilder();
    StringBuilder values = new StringBuilder();

    sql.append("INSERT INTO ")
        .append(getNameOpening())
        .append(StringUtils.convertCamelToSnake(table))
        .append(getNameEnding())
        .append(" (");

    boolean first = true;
    for (String key : object.keySet()) {
      JsonElement element = object.get(key);
      String normalizedKey = getNameOpening() + StringUtils.convertCamelToSnake(key) + getNameEnding();

      if (!first) {
        sql.append(", ");
        values.append(", ");
      }
      first = false;

      sql.append(normalizedKey);

      if (element.isJsonNull()) {
        values.append("NULL");
      } else if (element.isJsonPrimitive()) {
        if (element.getAsJsonPrimitive().isString()) {
          values.append(getValueOpening())
              .append(element.getAsJsonPrimitive().getAsString().replace("'", "''"))
              .append(getValueEnding());
        } else if (element.getAsJsonPrimitive().isBoolean()) {
          values.append(element.getAsBoolean() ? "TRUE" : "FALSE");
        } else {
          values.append(element.getAsJsonPrimitive().toString());
        }
      } else {
        values.append(getValueOpening())
            .append(element.toString().replace("'", "''"))
            .append(getValueEnding());
      }
    }

    sql.append(")\nVALUES (").append(values).append(")");
    return sql.toString();
  }

  @Override
  public String generateGet(String tableName, String[] columns, String condition) throws SQLException {
    if (tableName == null || tableName.isEmpty())
      throw new SQLException("Table name must not be null");

    StringBuilder sb = new StringBuilder("SELECT ");
    if (columns == null || columns.length == 0) {
      sb.append("*");
    } else {
      boolean isFirst = true;
      for (String column : columns) {
        if (!isFirst)
          sb.append(", ");
        isFirst = false;
        sb.append(getNameOpening())
            .append(StringUtils.convertCamelToSnake(column))
            .append(getNameEnding());
      }
    }

    sb.append(" FROM ")
        .append(getNameOpening())
        .append(StringUtils.convertCamelToSnake(tableName))
        .append(getNameEnding());

    if (condition != null && !condition.trim().isEmpty()) {
      sb.append(" ").append(condition);
    }

    return sb.toString();
  }

  @Override
  public String generateCount(String tableName, String condition) throws SQLException {
    if (tableName == null || tableName.isEmpty())
      throw new SQLException("Table name must not be null");

    StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM ")
        .append(getNameOpening())
        .append(StringUtils.convertCamelToSnake(tableName))
        .append(getNameEnding());

    if (condition != null && !condition.trim().isEmpty()) {
      sb.append(" ").append(condition);
    }

    return sb.toString();
  }

  @Override
  public String generateUpdate(String tableName, Object data, String where) throws SQLException {
    if (tableName == null || tableName.trim().isEmpty())
      throw new SQLException("Table name cannot be null or empty");
    if (data == null)
      throw new SQLException("Data object cannot be null");

    JsonObject jsonObject = Database.gson.toJsonTree(data).getAsJsonObject();
    StringBuilder sql = new StringBuilder();
    StringBuilder setClause = new StringBuilder();

    sql.append("UPDATE ")
        .append(getNameOpening())
        .append(StringUtils.convertCamelToSnake(tableName))
        .append(getNameEnding())
        .append(" SET ");

    boolean first = true;
    for (String key : jsonObject.keySet()) {
      JsonElement element = jsonObject.get(key);
      if (!first)
        setClause.append(", ");
      first = false;

      setClause.append(getNameOpening())
          .append(StringUtils.convertCamelToSnake(key))
          .append(getNameEnding())
          .append("=");

      if (element.isJsonNull()) {
        setClause.append("NULL");
      } else if (element.isJsonPrimitive()) {
        if (element.getAsJsonPrimitive().isString()) {
          setClause.append(getValueOpening())
              .append(element.getAsJsonPrimitive().getAsString().replace("'", "''"))
              .append(getValueEnding());
        } else if (element.getAsJsonPrimitive().isBoolean()) {
          setClause.append(element.getAsBoolean() ? "TRUE" : "FALSE");
        } else {
          setClause.append(element.getAsJsonPrimitive().toString());
        }
      } else {
        setClause.append(getValueOpening())
            .append(element.toString().replace("'", "''"))
            .append(getValueEnding());
      }
    }

    sql.append(setClause);
    if (where != null && !where.trim().isEmpty()) {
      sql.append("\nWHERE ").append(where.trim());
    }

    return sql.toString();
  }

  @Override
  public String generateDelete(String tableName, String where) throws SQLException {
    if (tableName == null || tableName.trim().isEmpty())
      throw new SQLException("Table name cannot be null or empty");

    StringBuilder sql = new StringBuilder("DELETE FROM ")
        .append(getNameOpening())
        .append(StringUtils.convertCamelToSnake(tableName))
        .append(getNameEnding());

    if (where != null && !where.trim().isEmpty()) {
      sql.append(" WHERE ").append(where.trim());
    }

    return sql.toString();
  }

  private String generateFieldSql(Field field) throws SQLException {
    Column column = field.getAnnotation(Column.class);
    String columnName = field.getName();
    String columnType = getColumnType(field);

    StringBuilder sb = new StringBuilder("\t")
        .append(getNameOpening())
        .append(StringUtils.convertCamelToSnake(columnName))
        .append(getNameEnding())
        .append(" ")
        .append(columnType);

    if (column != null) {
      if (!column.nullable())
        sb.append(" NOT NULL");
      if (column.unique())
        sb.append(" UNIQUE");
      if (!column.defaultValue().isEmpty()) {
        sb.append(" DEFAULT ").append(column.defaultValue().equals("0") ? "0" : "'" + column.defaultValue() + "'");
      }
    }

    Identity identity = field.getAnnotation(Identity.class);
    if (identity != null && identity.type() == IdType.PRIMITIVE && ClassUtils.isPrimitive(field.getType())) {
      sb.append(" AUTO_INCREMENT");
    }

    return sb.toString();
  }
}