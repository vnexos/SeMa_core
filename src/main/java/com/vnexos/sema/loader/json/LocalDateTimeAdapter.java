package com.vnexos.sema.loader.json;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A specialized {@code TypeAdapter<LocalDateTime>} to helps Gson factory handle
 * with LocalDateTime type.
 * 
 * @author Trần Việt Đăng Quang
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @Override
  public LocalDateTime read(JsonReader in) throws IOException {
    String data = in.nextString();
    if (data.equals("null"))
      return null;
    return LocalDateTime.parse(data, formatter);
  }

  @Override
  public void write(JsonWriter out, LocalDateTime value) throws IOException {
    if (value == null)
      out.value((String) null);
    else
      out.value(value.format(formatter));
  }
}
