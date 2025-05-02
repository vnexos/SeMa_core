package com.vnexos.sema.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements necessary utilities for interacting with java strings.
 * 
 * @author Trần Việt Đăng Quang
 */
public class StringUtils {
  /**
   * Converts from CamelCase to snake_case
   * 
   * @param str the string to convert
   * @return the converted string
   */
  public static String convertCamelToSnake(String str) {
    return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
  }

  /**
   * Converts from snake_case to CamelCase
   * 
   * @param str the string to convert
   * @return the converted string
   */
  public static String convertSnakeToCamel(String str) {
    StringBuilder result = new StringBuilder();
    boolean upperNext = false;

    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c == '_') {
        upperNext = true;
      } else {
        if (upperNext) {
          result.append(Character.toUpperCase(c));
          upperNext = false;
        } else {
          result.append(Character.toLowerCase(c));
        }
      }
    }

    return result.toString();
  }

  /**
   * Split CamelCase into string array
   * 
   * @param str the string to process
   * @return the array of parts in string
   */
  public static String[] splitCamelCase(String str) {
    return convertCamelToSnake(str).split("_");
  }

  /**
   * Converts query string to map
   * 
   * @param query the query string
   * @return map of key-value in query string
   */
  public static Map<String, String> queryToMap(String query) {
    Map<String, String> params = new HashMap<>();
    if (query != null && !query.isEmpty()) {
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        String[] keyValue = pair.split("=", 2); // Split on first '=' only
        if (keyValue.length == 2) {
          String key = keyValue[0];
          String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
          params.put(key, value);
        }
      }
    }

    return params;
  }
}
