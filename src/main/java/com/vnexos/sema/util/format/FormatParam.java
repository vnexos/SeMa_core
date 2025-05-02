package com.vnexos.sema.util.format;

/**
 * Represents the param of format functions in string.
 * 
 * @author Trần Việt Đăng Quang
 */
public class FormatParam {
  private Class<?> type;
  private Object value;

  /**
   * Constructs a FormatParam.
   * 
   * @param type  type of the param
   * @param value value of the param
   */
  public FormatParam(Class<?> type, Object value) {
    this.type = type;
    this.value = value;
  }

  /**
   * Gets type of param.
   * 
   * @return the param type
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * Sets type of param.
   * 
   * @param type the type to set
   */
  public void setType(Class<?> type) {
    this.type = type;
  }

  /**
   * Gets value of param.
   * 
   * @return param value
   */
  public Object getValue() {
    return value;
  }

  /**
   * Sets value of param.
   * 
   * @param value the value to set
   */
  public void setValue(Object value) {
    this.value = value;
  }
}
