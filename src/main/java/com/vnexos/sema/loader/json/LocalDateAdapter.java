package com.vnexos.sema.loader.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A specialized {@code TypeAdapter<LocalDate>} to helps Gson factory handle
 * with LocalDate type.
 * 
 * @author Trần Việt Đăng Quang
 */
public class LocalDateAdapter extends TypeAdapter<LocalDate> {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

  @Override
  public LocalDate read(JsonReader in) throws IOException {
    String data = in.nextString();
    if (data.equals("null"))
      return null;
    return LocalDate.parse(data, formatter);
  }

  @Override
  public void write(JsonWriter out, LocalDate value) throws IOException {
    if (value == null)
      out.value((String) null);
    else
      out.value(value.format(formatter));
  }

}
