package com.vnexos.sema.loader.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a field that will be automatically assigned the associated object.
 * The type of the auto-wired can be either a {@code DatabaseContext}
 * (repository) or a Service (annotated with {@code &#064;Service}). The fields
 * with {@code &#064;AutoWired} can be every where in the module project.
 * 
 * <p>
 * For example:
 * 
 * <pre>
 * &#064;AutoWired
 * ITranslationRepository translationRepository;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoWired {
}
