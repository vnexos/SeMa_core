package com.vnexos.sema.loader.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a parameter of a Route as a part of HTTP request.
 * 
 * <p>
 * The annotated parameter can only be a part of the route.
 * 
 * @author Trần Việt Đăng Quang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface FromRoute {
}
