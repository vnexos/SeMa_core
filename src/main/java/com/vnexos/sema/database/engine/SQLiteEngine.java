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
 * Provides engine for SQLite database
 * 
 * @author Trần Việt Đăng Quang
 * @see DatabaseEngine
 */
public class SQLiteEngine implements DatabaseEngine {

  private enum ColumnType {
    INTEGER("INTEGER"),
    REAL("REAL"),
    TEXT("TEXT"),
    BLOB("BLOB"),
    NUMERIC("NUMERIC");

    private final String type;

    ColumnType(String type) {
      this.type = type;
    }

    String getType() {
      return type;
    }
  }

  private static final Map<Class<?>, TypeMapping> TYPE_MAPPINGS = Map.ofEntries(
      Map.entry(boolean.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(Boolean.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(byte.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(Byte.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(short.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(Short.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(int.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(Integer.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(long.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(Long.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(java.math.BigInteger.class, new TypeMapping(ColumnType.INTEGER)),
      Map.entry(float.class, new TypeMapping(ColumnType.REAL)),
      Map.entry(Float.class, new TypeMapping(ColumnType.REAL)),
      Map.entry(double.class, new TypeMapping(ColumnType.REAL)),
      Map.entry(Double.class, new TypeMapping(ColumnType.REAL)),
      Map.entry(char.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(Character.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(String.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(UUID.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(java.util.Date.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(java.sql.Date.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(java.time.LocalDate.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(java.sql.Time.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(java.time.LocalTime.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(java.sql.Timestamp.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(java.time.LocalDateTime.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(java.time.Instant.class, new TypeMapping(ColumnType.TEXT)),
      Map.entry(byte[].class, new TypeMapping(ColumnType.BLOB)),
      Map.entry(Byte[].class, new TypeMapping(ColumnType.BLOB)),
      Map.entry(java.math.BigDecimal.class, new TypeMapping(ColumnType.NUMERIC)));

  private static class TypeMapping {
    final ColumnType columnType;

    TypeMapping(ColumnType columnType) {
      this.columnType = columnType;
    }
  }

  @Override
  public String getNameOpening() {
    return "\"";
  }

  @Override
  public String getNameEnding() {
    return "\"";
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
    return DatabaseType.SQLITE;
  }

  @Override
  public String getColumnType(Field field) throws SQLException {
    Column column = field.getAnnotation(Column.class);
    String customType = (column != null && !column.type().isEmpty()) ? column.type().toUpperCase() : "";

    if (!customType.isEmpty()) {
      return getCustomColumnType(customType);
    }
    return getTypeBasedColumnType(field.getType());
  }

  private String getCustomColumnType(String customType) {
    switch (customType) {
      case "INTEGER":
        return ColumnType.INTEGER.getType();
      case "REAL":
        return ColumnType.REAL.getType();
      case "TEXT":
        return ColumnType.TEXT.getType();
      case "BLOB":
        return ColumnType.BLOB.getType();
      case "NUMERIC":
        return ColumnType.NUMERIC.getType();
      default:
        return customType;
    }
  }

  private String getTypeBasedColumnType(Class<?> type) {
    if (type.isEnum()) {
      return ColumnType.TEXT.getType();
    }

    TypeMapping mapping = TYPE_MAPPINGS.get(type);
    if (mapping != null) {
      return mapping.columnType.getType();
    }

    throw new IllegalArgumentException("Unsupported field type: " + type.getName());
  }

  @Override
  public String generateCreateDatabase(String databaseName) throws SQLException {
    // SQLite does not support CREATE DATABASE; databases are created by connecting
    // to a file
    return "";
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

    sql.append("\n)");
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
          values.append(element.getAsBoolean() ? "1" : "0"); // SQLite uses INTEGER for BOOLEAN
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
          setClause.append(element.getAsBoolean() ? "1" : "0"); // SQLite uses INTEGER for BOOLEAN
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
      sb.append(" PRIMARY KEY AUTOINCREMENT");
    }

    return sb.toString();
  }
}