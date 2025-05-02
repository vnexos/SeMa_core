package com.vnexos.sema.loader.interfaces;

import com.vnexos.sema.context.ModuleServerContext;

/**
 * Represents a main class of a Module.
 * 
 * @author Trần Việt Đăng Quang
 */
public abstract class AModule {
  public abstract void onEnabled(ModuleServerContext context);

  public abstract void onDisabled();
}
