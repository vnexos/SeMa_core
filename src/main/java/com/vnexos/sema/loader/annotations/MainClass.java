package com.vnexos.sema.loader.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a class as the main class of the module. This class must be the
 * only one of the module project.
 * 
 * @author Trần Việt Đăng Quang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MainClass {
  String value();
}
