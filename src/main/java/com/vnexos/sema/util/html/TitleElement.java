package com.vnexos.sema.util.html;

/**
 * Represents title element of HTML.
 * 
 * @author Trần Việt Đăng Quang
 */
public class TitleElement extends DOM {
  /**
   * Constructs title element.
   * 
   * @param title the content of title element
   */
  public TitleElement(String title) {
    super("title");
    addChildren(title);
  }
}
