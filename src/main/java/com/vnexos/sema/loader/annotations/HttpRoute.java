package com.vnexos.sema.loader.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vnexos.sema.loader.HttpMethod;

/**
 * Specifies an annotation for routes
 * 
 * @author Trần Việt Đăng Quang
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpRoute {
  public HttpMethod value();
}
