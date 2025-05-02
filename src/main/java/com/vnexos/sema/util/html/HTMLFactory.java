package com.vnexos.sema.util.html;

/**
 * Helps building HTML.
 * 
 * @author Trần Việt Đăng Quang
 */
public class HTMLFactory {
  /**
   * Init the HTML
   * 
   * @return the HTML element
   */
  public static DOM init() {
    return new HTMLElement();
  }
}
