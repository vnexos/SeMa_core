/*
 * Copyright (c) 2025, VNExos and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package com.vnexos.sema.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the mapping of a field to a database column.
 * When applied to a field, indicates that the field should be presited
 * to a database column with the specified characteristics.
 * 
 * <p>
 * For example:
 * 
 * <pre>
 * &#064;Entity(tableName = "some_entity")
 * class SomeEntity {
 *   &#064;Column
 *   private String value;
 * 
 *   &#064;Column(nullable = false, type="TEXT")
 *   private String another value;
 * 
 *   // ... default getter, setter ...
 * }
 * </pre>
 * 
 * @author Trần Việt Đăng Quang
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
  /**
   * Whether the column has a unique constraint.
   * 
   * @return true if the column values must be unique, false otherwise
   */
  boolean unique() default false;

  /**
   * Whether the column allows null values.
   * 
   * @return true if null value is permitted, false otherwise
   */
  boolean nullable() default true;

  /**
   * The SQL data type for the column.
   * <p>
   * If not specified, the type will be inferred from the Java field type.
   * 
   * @return the explicit column type definition
   */
  String type() default "";

  /**
   * The default value for the column.
   * 
   * @return the default value as a string representation
   */
  String defaultValue() default "";

  /**
   * The maximun length for columns.
   * 
   * @return the maximun allowed length of the column
   */
  int length() default 256;

  /**
   * 
   */
  Class<?> foreignKey() default Object.class;
}
