package com.vnexos.sema.loader.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a field to be hidden while serialize an object to JSON.
 * 
 * <p>
 * For example:
 * 
 * <pre>
 * &#64;Entity(tableName = "languages")
 * public class Language extends DefaultEntity {
 *   &#64;Column(nullable = false)
 *   private String fullname;
 *
 *   &#64;Column(nullable = false, unique = true)
 *   private String username;
 *
 *   &#64;Column(nullable = false)
 *   &#64;Hidden
 *   private String password;
 * 
 *   // ---- getter, setter
 * }
 * </pre>
 * 
 * @author Trần Việt Đăng Quang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Hidden {
}
