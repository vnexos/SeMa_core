package com.vnexos.sema.loader.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vnexos.sema.loader.HttpMethod;

/**
 * Specifies a method as a route with PUT method of controller.
 * 
 * @author Trần Việt Đăng Quang
 * @see HttpRoute
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@HttpRoute(HttpMethod.PUT)
public @interface HttpPut {
  String value() default "";
}
