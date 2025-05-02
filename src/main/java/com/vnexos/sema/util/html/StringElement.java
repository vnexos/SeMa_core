package com.vnexos.sema.util.html;

/**
 * Represents text element of HTML.
 * 
 * @author Trần Việt Đăng Quang
 */
public class StringElement extends DOM {
  private String value;

  /**
   * Constructs a text element.
   * 
   * @param value the value of text
   */
  public StringElement(String value) {
    super(null);
    this.value = value;
  }

  /**
   * Disables add children
   */
  @Override
  public DOM addChildren(DOM... dom) {
    throw new UnsupportedOperationException("addChildren is not valid for String");
  }

  /**
   * Disables add children
   */
  @Override
  public DOM addChildren(String data) {
    throw new UnsupportedOperationException("addChildren is not valid for String");
  }

  /**
   * Disables set property
   */
  @Override
  public DOM setProperty(String key, String value) {
    throw new UnsupportedOperationException("setProperty is not valid for String");
  }

  /**
   * Get the value of String element.
   */
  @Override
  public String toString() {
    return value;
  }

}
