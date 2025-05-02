package com.vnexos.sema.database;

/**
 * Enumerates the supported id type.
 * 
 * <p>
 * The type of ID including:
 * <ul>
 * <li>{@link IdType#UUID IdType.UUID} - For the UUID type, the system will
 * generate a UUID at the INSERT time.
 * <li>{@link IdType#PRIMITIVE IdType.PRIMITIVE} - For the primitive types, the
 * system will add it to the CREATE TABLE statement.
 * </ul>
 * 
 * <p>
 * This will help the system decide to generate at CREATE TABLE time or INSERT
 * time
 * 
 * @author Trần Việt Đăng Quang
 */
public enum IdType {
  UUID,
  PRIMITIVE
}
