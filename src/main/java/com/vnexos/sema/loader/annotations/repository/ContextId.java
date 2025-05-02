package com.vnexos.sema.loader.annotations.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a parameter in a method of the repository as the id of the entity
 * related to the repository.
 * 
 * <p>
 * This is usualy used for {@code update()} method. For example:
 * 
 * <pre>
 * public Entity update(&#064;ContextId UUID id, String flagUrl);
 * </pre>
 * 
 * @author Trần Việt Đăng Quang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ContextId {
}
