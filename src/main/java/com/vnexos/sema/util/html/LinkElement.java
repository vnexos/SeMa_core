package com.vnexos.sema.util.html;

public class LinkElement extends DOM {

  public LinkElement(String rel, String type) {
    this();
    setProperty("rel", rel);
    setProperty("type", type);
  }

  public LinkElement() {
    super("link");
  }

}
