package com.vnexos.sema.database;

/**
 * Enumerates the helper type of entity
 * 
 * <p>
 * The supported type including:
 * <ul>
 * <li>{@link HelperType#CREATED_AT HelperType.CREATED_AT} - generate the
 * current INSERT time.
 * <li>{@link HelperType#UPDATED_AT HelperType.UPDATED_AT} - generate the
 * current UPDATE time.
 * <li>{@link HelperType#DELETED_AT HelperType.DELETED_AT} - generate the
 * current DELETE time. (not recommend)
 * </ul>
 * 
 * <p>
 * This class will help the system generate the necessary data for database at
 * the INSERT, UPDATE time.
 *
 * @author Trần Việt Đăng Quang
 */
public enum HelperType {
  CREATED_AT,
  UPDATED_AT,
  @Deprecated
  DELETED_AT,
}
