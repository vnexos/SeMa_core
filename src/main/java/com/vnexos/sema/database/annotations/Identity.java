/*
 * Copyright (c) 2025, VNExos and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package com.vnexos.sema.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vnexos.sema.database.IdType;

/**
 * Specifies a field of an entity class as an identity (or primary key)
 * of the table that matches with the entity.
 * 
 * <p>
 * There are 2 types of Id and be defined in {@link IdType
 * com.vnexos.sema.database.IdType}:
 * <ul>
 * <li>The {@link IdType#PRIMITIVE IdType.PRIMITIVE} is for the id in integer
 * type. For example:
 * 
 * <pre>
 * &#064;Identity(type = IdType.PRIMITIVE)
 * &#064;Column
 * private long id;
 * </pre>
 * 
 * <li>The {@link IdType#UUID IdType.UUID} is for the UUID type, and
 * to tell to the system to generate the UUID base on the system
 * algorithm at the INSERT time. For example:
 * 
 * <pre>
 * &#064;Identity(type = IdType.UUID)
 * &#064;Column(type = "uniqueidentifier")
 * private UUID id;
 * </pre>
 * </ul>
 * 
 * <p>
 * The UUID algorithm is the UUID version 6 (time based-generated UUID). See
 * more at {@link com.vnexos.sema.util.UUIDUtil#v6()
 * com.vnexos.sema.util.UUIDUtil.v6()}
 * 
 * @author Trần Việt Đăng Quang
 * @see IdType
 * @see com.vnexos.sema.util.UUIDUtil
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Identity {
  /**
   * Store the type of ID
   * <ul>
   * <li>{@link IdType#UUID IdType.UUID} will help the system generate the UUID in
   * the time inserting into the database table
   * <li>{@link IdType#PRIMITIVE IdType.PRIMITIVE} will help the database table
   * auto increase its id in table create time
   * </ul>
   * <p>
   * The UUID algorithm is the UUID version 6 (time based-generated UUID). See
   * more at {@link com.vnexos.sema.util.UUIDUtil#v6()
   * com.vnexos.sema.util.UUIDUtil.v6()}
   * 
   * @return the type of Id field
   */
  IdType type() default IdType.PRIMITIVE;
}
