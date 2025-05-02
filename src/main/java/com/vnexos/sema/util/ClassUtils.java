package com.vnexos.sema.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements necessary utilities for interacting with java classes.
 * 
 * @author Trần Việt Đăng Quang
 */
public class ClassUtils {
  /**
   * Private constructor to avoid creating an instance of ClassUtils
   */
  private ClassUtils() {
  }

  /**
   * Gets all fields of a Class
   * 
   * @param clazz the class to process
   * @return the list of fields
   */
  public static List<Field> getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();

    for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
      for (Field field : current.getDeclaredFields()) {
        fields.add(field);
      }
    }

    return fields;
  }

  /**
   * Checks if a type is primitive.
   * 
   * @param type the type to check
   * @return true if the type is primitive, false otherwise
   */
  public static boolean isPrimitive(Class<?> type) {
    return type.isPrimitive() ||
        type == Integer.class ||
        type == Long.class ||
        type == Double.class ||
        type == Float.class ||
        type == Boolean.class ||
        type == Character.class ||
        type == Byte.class ||
        type == Short.class;
  }

  /**
   * Check if the given list of classes contains the given class.
   * 
   * @param interfaces the array of class
   * @param clazz      the class to check
   * @return true if the given class is contained in the given array, false
   *         otherwise
   */
  public static boolean checkClassesContain(Class<?>[] interfaces, Class<?> clazz) {
    if (interfaces == null || interfaces.length == 0)
      return false;
    for (Class<?> i : interfaces) {
      if (i == clazz)
        return true;
    }
    return false;
  }

  /**
   * Get all enum values as String
   * 
   * @param enumType the class type of enum
   * @return the list of enum values in string type
   */
  @SuppressWarnings("unchecked")
  public static String[] getEnumValues(Class<?> enumType) {
    return Arrays.asList(
        ((Class<? extends Enum<?>>) enumType)
            .getEnumConstants())
        .stream()
        .map(Enum::name)
        .toArray(String[]::new);
  }

  /**
   * Find a fields inside the class which have specific annotation.
   * 
   * @param <T>        the type of annotation
   * @param entity     the entity contains fields
   * @param annotation the specific annotation
   * @return the first Field occures which contains specific annotation
   */
  public static <T extends Annotation> Field findAnnotatedField(Class<?> entity, Class<T> annotation) {
    for (Class<?> current = entity; current != null && current != Object.class; current = current.getSuperclass()) {
      for (Field field : current.getDeclaredFields()) {
        if (field.getAnnotation(annotation) != null)
          return field;
      }
    }

    return null;
  }
}
