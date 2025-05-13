package com.vnexos.sema.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vnexos.sema.loader.json.HiddenExclusionStrategy;
import com.vnexos.sema.loader.json.LocalDateAdapter;
import com.vnexos.sema.loader.json.LocalDateTimeAdapter;
import com.vnexos.sema.loader.json.LocalTimeAdapter;

/**
 * Implements necessary utilities for Mapping data.
 * <p>
 * The Mapper use Gson as the central to map 2 type of data.
 * 
 * 
 */
public class Mapper {
  private static final Gson gson = new GsonBuilder()
      .setExclusionStrategies(new HiddenExclusionStrategy())
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
      .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
      .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
      .create();

  /**
   * Maps the source to the specific type.
   * 
   * @param <S>         the type of source
   * @param <D>         the type of destination
   * @param source      the data source
   * @param destination the destination type
   * @return the mapped data in destination type
   */
  @SuppressWarnings("unchecked")
  public static <S, D> D map(S source, Class<D> destination) {
    if (source == null)
      return null;

    if (destination.isAssignableFrom(source.getClass())) {
      return (D) source;
    }

    if (destination == String.class) {
      return (D) gson.toJson(source);
    }

    String json = source instanceof String ? (String) source : gson.toJson(source);
    return gson.fromJson(json, destination);
  }

  /**
   * Serializes {@code ResultSet} value to {@code JsonArray}
   * 
   * @param rs the result set value
   * @return the {@code JsonArray} serialized from result set
   * @throws SQLException if an error occurs while processing {@code ResultSet}
   */
  public static JsonArray serializeResultSet(ResultSet rs) throws SQLException {
    JsonArray array = new JsonArray();
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    while (rs.next()) {
      JsonObject jsonObject = new JsonObject();
      for (int i = 1; i <= columnCount; i++) {
        String column = StringUtils.convertSnakeToCamel(metaData.getColumnLabel(i));
        Object value = rs.getObject(i);

        if (value == null) {
          jsonObject.addProperty(column, (String) null);
        } else if (value instanceof Integer) {
          jsonObject.addProperty(column, (Integer) value);
        } else if (value instanceof Boolean) {
          jsonObject.addProperty(column, (Boolean) value);
        } else if (value instanceof Long) {
          jsonObject.addProperty(column, (Long) value);
        } else if (value instanceof Double) {
          jsonObject.addProperty(column, (Double) value);
        } else {
          jsonObject.addProperty(column, value.toString());
        }
      }
      array.add(jsonObject);
    }
    return array;
  }
}
