package com.vnexos.sema.util.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides list to store all params of a format function. The instance of this
 * can be used in for-each loops.
 * 
 * <p>
 * For example:
 * 
 * <pre>
 * for (FormatParam formatParam : formatParamList) {
 *   // Do something with formatParam
 * }
 * </pre>
 * 
 * @author Trần Việt Đăng Quang
 * @see FormatParam
 */
public class FormatParamList implements Iterable<FormatParam> {
  private List<FormatParam> colorParams;

  /**
   * Constructs a list of format param.
   */
  public FormatParamList() {
    colorParams = new ArrayList<>();
  }

  /**
   * Adds type and object into the list.
   * 
   * @param type  the type of parameter
   * @param value the value of parameter
   */
  public void add(Class<?> type, Object value) {
    colorParams.add(new FormatParam(type, value));
  }

  /**
   * Adds a FormatParam instance into the list.
   * 
   * @param colorParam the FormatParam instance to add.
   */
  public void add(FormatParam colorParam) {
    colorParams.add(colorParam);
  }

  /**
   * Gets param at the index.
   * 
   * @param index the position of FormatParam in the list
   * @return the FormatParam instance at the given index
   */
  public FormatParam get(int index) {
    return colorParams.get(index);
  }

  /**
   * Gets all param types.
   * 
   * @return list of param types
   */
  public Class<?>[] getTypes() {
    Class<?>[] types = new Class[colorParams.size()];
    int index = 0;
    for (FormatParam cp : colorParams) {
      types[index++] = cp.getType();
    }
    return types;
  }

  /**
   * Gets all param values.
   * 
   * @return list of param values
   */
  public Object[] getValues() {
    Object[] values = new Object[colorParams.size()];
    int index = 0;
    for (FormatParam cp : colorParams) {
      values[index++] = cp.getValue();
    }
    return values;
  }

  /**
   * Supports interating the list.
   */
  @Override
  public Iterator<FormatParam> iterator() {
    return colorParams.iterator();
  }

  /**
   * Gets size of the list.
   * 
   * @return the list size
   */
  public int size() {
    return colorParams.size();
  }
}
