package com.vnexos.sema.loader.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.vnexos.sema.loader.annotations.Hidden;

/**
 * A specialized {@code ExclusionStrategy} that associates hiding fields
 * annotated by {@code &#64;Hidden}.
 * 
 * @author Trần Việt Đăng Quang
 * @see Hidden
 */
public class HiddenExclusionStrategy implements ExclusionStrategy {
  @Override
  public boolean shouldSkipClass(Class<?> c) {
    return false;
  }

  @Override
  public boolean shouldSkipField(FieldAttributes f) {
    return f.getAnnotation(Hidden.class) != null;
  }
}
