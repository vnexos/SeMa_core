package com.vnexos.sema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vnexos.sema.context.ServerContext;
import com.vnexos.sema.loader.json.HiddenExclusionStrategy;
import com.vnexos.sema.loader.json.LocalDateAdapter;
import com.vnexos.sema.loader.json.LocalDateTimeAdapter;
import com.vnexos.sema.loader.json.LocalTimeAdapter;
import com.vnexos.sema.loader.json.ReorderFactory;

/**
 * Contains global variables and methods to get data from config file.
 * 
 * @author Trần Việt Đăng Quang
 */
public class Constants {
  private Constants() {
  }

  protected static final Properties props = new Properties();

  public static boolean getBoolean(String str) {
    return Boolean.parseBoolean(props.getProperty(str));
  }

  public static String getString(String str) {
    return props.getProperty(str);
  }

  public static Integer getInteger(String str) {
    return Integer.parseInt(props.getProperty(str));
  }

  public static ServerContext context;

  public static final Gson gson = new GsonBuilder()
      .serializeNulls()
      .setExclusionStrategies(new HiddenExclusionStrategy())
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
      .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
      .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
      .registerTypeAdapterFactory(new ReorderFactory())
      .create();
}
