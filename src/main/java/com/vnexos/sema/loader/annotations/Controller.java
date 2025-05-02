package com.vnexos.sema.loader.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies class as a controller and each method with the specific annotation
 * is a route of the controller.
 * 
 * @author Trần Việt Đăng Quang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
  /**
   * Gets the path of the controller
   * 
   * @return the controller path
   */
  String value() default "/";
}
