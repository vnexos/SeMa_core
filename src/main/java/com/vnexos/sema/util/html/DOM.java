package com.vnexos.sema.util.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stands for Document Object Model, represents a node of HTML.
 * 
 * @author Trần Việt Đăng Quang
 */
public class DOM {
  private String tagName;
  private Map<String, String> props;
  private List<DOM> children;
  private DOM parent;

  /**
   * Constructs the DOM with the tagName.
   * 
   * @param tagName name of tag
   */
  public DOM(String tagName) {
    this.tagName = tagName;
    children = new ArrayList<>();
    props = new HashMap<>();
  }

  /**
   * Get props of tag
   * 
   * @return tag properties
   */
  protected Map<String, String> getProps() {
    return props;
  }

  /**
   * Get children
   * 
   * @return children of tag
   */
  protected List<DOM> getChildren() {
    return children;
  }

  /**
   * Sets parrent for a node of HTML.
   * 
   * @param parent the parent node to set
   */
  public void setParent(DOM parent) {
    this.parent = parent;
  }

  /**
   * Gets parent node of the current node.
   * 
   * @return the parent node
   */
  public DOM getParent() {
    return parent;
  }

  /**
   * Gets tag name.
   * 
   * @return the name of tag
   */
  public String getTagName() {
    return tagName;
  }

  /**
   * Sets tag name.
   * 
   * @param tagName the name of tag to set
   */
  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  /**
   * Sets property for the element.
   * 
   * @param key   the property key
   * @param value the property value
   * @return the current node itself
   */
  public DOM setProperty(String key, String value) {
    props.put(key, value);
    return this;
  }

  /**
   * Adds children into current node.
   * 
   * @param doms array of children node to add
   * @return the current node itself
   */
  public DOM addChildren(DOM... doms) {
    for (DOM dom : doms) {
      children.add(dom);
      dom.setParent(this);
    }
    return this;
  }

  /**
   * Adds text into current node.
   * 
   * @param data the text to add into node
   * @return the current node itself
   */
  public DOM addChildren(String data) {
    DOM se = new StringElement(data);
    se.setParent(this);
    children.add(se);
    return this;
  }

  /**
   * Converts current node and child nodes to string
   */
  @Override
  public String toString() {
    StringBuilder html = new StringBuilder();
    if (children.size() == 0) {
      html
          .append('<')
          .append(tagName);
      for (Map.Entry<String, String> entry : props.entrySet()) {
        html
            .append(' ')
            .append(entry.getKey())
            .append('=')
            .append('"')
            .append(entry.getValue())
            .append('"');
      }
      html.append(" />");
    } else {

      html
          .append('<')
          .append(tagName);
      for (Map.Entry<String, String> entry : props.entrySet()) {
        html
            .append(' ')
            .append(entry.getKey())
            .append('=')
            .append('"')
            .append(entry.getValue())
            .append('"');
      }
      html.append('>');
      for (DOM child : children)
        html.append(child);

      html.append("</")
          .append(tagName)
          .append('>');
    }
    return html.toString();
  }
}
