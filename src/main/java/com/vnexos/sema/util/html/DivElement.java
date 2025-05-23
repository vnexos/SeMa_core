package com.vnexos.sema.util.html;

import java.util.Map;

/**
 * Represents div element of HTML.
 * 
 * @author Trần Việt Đăng Quang
 */
public class DivElement extends DOM {
  /**
   * Constructs a div element
   */
  public DivElement() {
    super("div");
  }

  @Override
  public String toString() {
    StringBuilder html = new StringBuilder();
    html
        .append('<')
        .append(getTagName());
    for (Map.Entry<String, String> entry : getProps().entrySet()) {
      html
          .append(' ')
          .append(entry.getKey())
          .append('=')
          .append('"')
          .append(entry.getValue())
          .append('"');
    }
    html.append('>');
    for (DOM child : getChildren())
      html.append(child);

    html.append("</")
        .append(getTagName())
        .append('>');
    return html.toString();
  }
}
