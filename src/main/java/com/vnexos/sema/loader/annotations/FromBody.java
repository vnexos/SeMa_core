package com.vnexos.sema.loader.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a parameter of a Route as the body of HTTP request.
 * 
 * <p>
 * The annotated parameter can only be an object.
 * 
 * @author Trần Việt Đăng Quang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface FromBody {
}
