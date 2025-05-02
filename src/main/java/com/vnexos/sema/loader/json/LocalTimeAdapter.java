package com.vnexos.sema.loader.json;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A specialized {@code TypeAdapter<LocalTime>} to helps Gson factory handle
 * with LocalTime type.
 * 
 * @author Trần Việt Đăng Quang
 */
public class LocalTimeAdapter extends TypeAdapter<LocalTime> {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

  @Override
  public LocalTime read(JsonReader in) throws IOException {
    return LocalTime.parse(in.nextString(), formatter);
  }

  @Override
  public void write(JsonWriter out, LocalTime value) throws IOException {
    if (value == null)
      out.value((String) null);
    else
      out.value(value.format(formatter));
  }

}
