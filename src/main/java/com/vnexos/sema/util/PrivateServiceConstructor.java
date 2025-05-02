package com.vnexos.sema.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Handles interacting with private element inside a Java class.
 * 
 * @author Trần Việt Đăng Quang
 */
public class PrivateServiceConstructor {
  /**
   * Creates an instance from private constructor
   * 
   * @param <T>        the type of instance
   * @param clazz      the class contains constructor
   * @param paramTypes the array of parameter types of constructor
   * @param params     the array of parameter values for constructor
   * @return the value instance
   */
  @SuppressWarnings("unchecked")
  public static <T extends Object> T createInstance(Class<T> clazz, Class<?>[] paramTypes, Object[] params)
      throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
    constructor.setAccessible(true);

    return (T) constructor.newInstance(params);
  }

  /**
   * Invokes a private method.
   * 
   * @param clazz      the class that contain method
   * @param methodName the method name
   * @param instance   instance of the class
   * @param paramTypes the array of parameter types of constructor
   * @param params     the array of parameter values for constructor
   * @return the return value of the function
   */
  public static Object invokeFunction(Class<?> clazz, String methodName, Object instance, Class<?>[] paramTypes,
      Object[] params)
      throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
    Method method = clazz.getDeclaredMethod(methodName, paramTypes);
    method.setAccessible(true);

    return method.invoke(instance, params);
  }

  /**
   * Converts from {@code Class<?>...} to {@code Class<?>[]}
   * 
   * @param paramTypes the array of classes
   * @return the array of classes
   */
  public static Class<?>[] createClassTypes(Class<?>... paramTypes) {
    return paramTypes;
  }

  /**
   * Converts from {@code Object...} to {@code Object[]}
   * 
   * @param params the array of values
   * @return the array of values
   */
  public static Object[] createObjects(Object... params) {
    return params;
  }
}
