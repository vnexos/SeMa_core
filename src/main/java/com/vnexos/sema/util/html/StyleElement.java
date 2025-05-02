package com.vnexos.sema.util.html;

/**
 * Represents style element of HTML.
 * 
 * @author Trần Việt Đăng Quang
 */
public class StyleElement extends DOM {
  /**
   * Construct a style element.
   * 
   * @param style the content of style element
   */
  public StyleElement(String style) {
    super("style");
    addChildren(style);
  }
}
