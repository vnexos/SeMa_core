package com.vnexos.sema.util.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Formats the string by the implemented formatter function inside string.
 * 
 * <p>
 * There is 2 ways to construct a formatter:
 * <ol>
 * <li>Construct with default formatter driver:
 * 
 * <pre>
 * Formatter formatter = new Formatter();
 * formatter.format("$fg(1)$bg(15)Hello World!");
 * </pre>
 * 
 * This code will format the {@code "Hello World!"} on the white background and
 * red text.
 * <li>Construct with custom formatter driver:
 * <p>
 * Create CustomFormatterDriver:
 * 
 * <pre>
 * public class CustomFormatterDriver {
 *   public String name() {
 *     return "Quang";
 *   }
 * 
 *   public String punctual(String c) {
 *     return c;
 *   }
 * }
 * </pre>
 * 
 * <p>
 * Construct the formatter:
 * 
 * <pre>
 * Formatter formatter = new Formatter(CustomFormatterDriver.class);
 * formatter.format("Hello, $name()$punctual(!)");
 * </pre>
 * 
 * The result of this code is {@code "Hello, Quang!"}
 * </ol>
 * 
 * @author Trần Việt Đăng Quang
 * @see FormatDriver
 */
public class Formatter {
  private static final String ANSI_ESCAPE_CODE_PATTERN = "\u001B\\[[;\\d]*m";

  Class<?> formatDriver;

  /**
   * Constructs a formatter with default driver
   */
  public Formatter() {
    formatDriver = FormatDriver.class;
  }

  /**
   * Constructs a formatter with custom driver
   * 
   * @param formatDriver custom format driver
   */
  public Formatter(Class<?> formatDriver) {
    this.formatDriver = formatDriver;
  }

  /**
   * Checks if string is double
   * 
   * @param num the string to check
   * @return true if the string is in double format, false otherwise
   */
  private boolean isDouble(String num) {
    try {
      if (!num.endsWith("f"))
        return false;
      num = num.substring(0, num.length() - 1);
      Double.parseDouble(num);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Checks if string is integer
   * 
   * @param num the string to check
   * @return true if the string is in integer format, false otherwise
   */
  private boolean isInteger(String num) {
    try {
      Integer.parseInt(num);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Converts object to params.
   * 
   * @param obj the object to convert
   * @return the instance of FormatParam
   */
  private FormatParam generateColorParam(String obj) {
    if (isDouble(obj)) {
      return new FormatParam(double.class, Double.parseDouble(obj));
    } else if (isInteger(obj)) {
      return new FormatParam(int.class, Integer.parseInt(obj));
    } else {
      return new FormatParam(String.class, obj);
    }
  }

  /**
   * Execute the format function.
   * 
   * @param name the name of the function
   * @param list the list of params
   * @return the result after executing function
   * @throws NoSuchMethodException if the function name cannot be found in the
   *                               given driver
   */
  private String executeFunction(String name, FormatParamList list) throws NoSuchMethodException, SecurityException,
      InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Constructor<?> constructor = formatDriver.getConstructor();
    Object instance = constructor.newInstance();
    Method method = formatDriver.getMethod(name, list.getTypes());
    if (method.getReturnType() != String.class) {
      return null;
    }
    return (String) method.invoke(instance, list.getValues());
  }

  /**
   * Format the string.
   * 
   * @param format the string to format
   * @return the formatted string
   * @throws FormatException if string cannot be format
   */
  public String format(String format) throws FormatException {
    char[] s = format.toCharArray();
    String result = "";
    for (int i = 0; i < s.length; i++) {
      int begin = i;
      int end = 0;

      if (s[i] == '$') {
        i++;
        String methodName = "";

        // Get function name
        for (int j = i;; j++) {
          if (s[j] == '(' || j == s.length - 1) {
            i = j;
            break;
          }
          methodName += s[j];
        }
        i++;

        // Throw error when function parameter is not open with '('
        if (i == s.length) {
          throw new FormatException("Wrong function format: Function must have a pair of `(` and `)`.");
        }
        // Throw error when function parameter does not have a name
        if (methodName.length() == 0) {
          throw new FormatException("Wrong function format: Function must have a name.");
        }

        // Get all format function param
        String var = "";
        FormatParamList colorParams = new FormatParamList();
        for (int j = i;; j++) {
          if (s[j] == ')' || j == s.length) {
            i = j;
            var = var.trim();
            if (var.length() > 0)
              if (colorParams.size() == 0)
                colorParams.add(generateColorParam(var));
              else
                // Wrong param type
                throw new FormatException("Wrong function format: Cannot identify param at index " + colorParams.size()
                    + " in function " + methodName + ".");
            break;
          } else if (s[j] == ',') {
            j++;
            if (s[j] == ',') {
              var += s[j];
              continue;
            }

            var = var.trim();
            if (var.length() == 0)
              // param is null
              throw new FormatException("Wrong function format: Cannot identify param at index " + colorParams.size()
                  + " in function `" + methodName + "`.");

            colorParams.add(generateColorParam(var));
            var = "";
          }

          var += s[j];
        }
        // Throw if function does not end with ')'
        if (methodName.length() == 0 || i == s.length) {
          throw new FormatException("Wrong function format: Function must end with `)`.");
        }

        // End of a function
        end = i + 1;

        // execute format function
        try {
          result += executeFunction(methodName, colorParams);
        } catch (Exception e) {
          // if error occured, keep the function
          result += format.substring(begin, end);
        }
        continue;
      }

      // Add a normal char that does not belong to function
      result += s[i];
    }
    return result;
  }

  /**
   * Clear all format to log into file
   * 
   * @param message the message needed to append into file
   * @return the cleared message
   */
  public static String clearFormat(String message) {
    return message.replaceAll(ANSI_ESCAPE_CODE_PATTERN, "");
  }
}
