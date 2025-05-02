/*
 * Copyright (c) 2025, VNExos and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package com.vnexos.sema.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vnexos.sema.database.HelperType;

/**
 * Specifies the fields whom data will be generated while having an
 * action on records like CREATE, UPDATE with the type:
 * {@link HelperType#CREATED_AT} and {@link HelperType#UPDATED_AT}.
 * 
 * <p>
 * The type {@link HelperType#DELETED_AT} for soft deletion is not
 * recommended.
 * 
 * For example:
 * <p>
 * 
 * <pre>
 * ...
 * &#064;DataHelper(type = HelperType.CREATED_AT)
 * private LocalDateTime createdAt;
 * ...
 * </pre>
 * 
 * @author Trần Việt Đăng Quang
 * @see HelperType
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataHelper {
  /**
   * The type of data that be generated on action time
   * 
   * @return the helper type
   */
  public HelperType type();
}
