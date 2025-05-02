package com.vnexos.sema.util.logger.system;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.vnexos.sema.Constants;
import com.vnexos.sema.context.ServerContext;
import com.vnexos.sema.loader.Loader;
import com.vnexos.sema.loader.LoaderException;
import com.vnexos.sema.loader.Module;
import com.vnexos.sema.util.logger.FileLogger;
import com.vnexos.sema.util.logger.LoggerFormatDriver;
import com.vnexos.sema.util.logger.LoggerType;

/**
 * Handles system printing for {@code System.out} and {@code System.err}
 * 
 * @author Trần Việt Đăng Quang
 */
public class SystemPrintStream extends PrintStream {
  private FileLogger fileLogger;
  private GetMessageFunction getMessageFn;
  private static final int SPAN_STACK_INDEX = 3;
  private final boolean unknowAllowed;
  private LoggerType type;

  /**
   * Handles get the message with full of the information before print.
   * 
   * @param msg the message to process
   * @return the message after process
   */
  private String _getMessage(String msg) {
    if (unknowAllowed) {
      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
      try {
        Module module = Loader.findModule(elements[SPAN_STACK_INDEX].getClassName());
        LoggerFormatDriver.setModule(module.getModuleName());
        LoggerFormatDriver.setClassPath(elements[ServerContext.STACK_TRACE_INDEX]);
      } catch (LoaderException e) {
      }
      return getMessageFn.getMessage(String.valueOf(msg), type, SPAN_STACK_INDEX - 1);
    }
    return "";
  }

  /**
   * Resets the module after print a line from Module.
   */
  private void resetModule() {
    LoggerFormatDriver.setModule(null);
  }

  /**
   * Constructs a print stream for system printing.
   * 
   * @param out          the original out
   * @param fileLogger   the {@link FileLogger
   *                     com.vnexos.sema.util.logger.FileLogger} instance
   * @param getMessageFn the lambda function for getting message
   * @param type         the type of Logger
   * @throws UnsupportedEncodingException if the given encoding is not supported
   */
  public SystemPrintStream(OutputStream out, FileLogger fileLogger, GetMessageFunction getMessageFn, LoggerType type)
      throws UnsupportedEncodingException {
    super(out, true, "UTF-8");
    this.fileLogger = fileLogger;
    this.getMessageFn = getMessageFn;
    this.type = type;
    this.unknowAllowed = Arrays.stream(Constants.getString("log.level").split("\\|"))
        .anyMatch(l -> l.equals("UNKNOWN"));
  }

  /**
   * Prints the message to the screen.
   * 
   * @param msg the message to print
   */
  private void printLog(String msg) {
    if (unknowAllowed) {
      resetModule();
      super.println(msg);
      fileLogger.log(msg);
    }
  }

  @Override
  public void println() {
    String msg = _getMessage("");
    printLog(msg);
  }

  @Override
  public void println(char[] s) {
    if (s == null) {
      printLog("null");
      return;
    }
    String msg = _getMessage(String.valueOf(s));
    printLog(msg);
  }

  @Override
  public void println(Object x) {
    if (x == null) {
      printLog("null");
      return;
    }
    String msg = _getMessage(String.valueOf(x));
    printLog(msg);
  }

  @Override
  public void println(String x) {
    if (x == null) {
      printLog("null");
      return;
    }
    String msg = _getMessage(String.valueOf(x));
    printLog(msg);
  }

  @Override
  public void println(double x) {
    String msg = _getMessage(String.valueOf(x));
    printLog(msg);
  }

  @Override
  public void println(float x) {
    String msg = _getMessage(String.valueOf(x));
    printLog(msg);
  }

  @Override
  public void println(long x) {
    String msg = _getMessage(String.valueOf(x));
    printLog(msg);
  }

  @Override
  public void println(int x) {
    String msg = _getMessage(String.valueOf(x));
    printLog(msg);
  }

  @Override
  public void println(char x) {
    String msg = _getMessage(String.valueOf(x));
    printLog(msg);
  }

  @Override
  public void println(boolean x) {
    String msg = _getMessage(String.valueOf(x));
    printLog(msg);
  }
}
