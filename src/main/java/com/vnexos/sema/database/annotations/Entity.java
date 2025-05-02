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
 * Specifies the class as the database table.
 * <p>
 * For example:
 * 
 * <pre>
 * &#064;Entity(tableName = "entities")
 * public class SampleEntity {
 *   ...
 * }
 * </pre>
 * 
 * @author Trần Việt Đăng Quang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
  /**
   * The name of entity table
   * 
   * @return entity table name
   */
  String tableName();
}
