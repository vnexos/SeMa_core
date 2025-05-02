package com.vnexos.sema.loader.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a class as the service class of the module. The system will create
 * an instance associated with the service class. The field that have the same
 * type as the service class and be annotated with {@code &#64;AutoWired} will
 * automatically be assigned.
 * 
 * @author Trần Việt Đăng Quang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
}
