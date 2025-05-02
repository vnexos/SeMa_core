package com.vnexos.sema.util.logger.system;

import com.vnexos.sema.util.logger.LoggerType;

/**
 * Represents lambda of getting message.
 * 
 * @author Trần Việt Đăng Quang
 */
@FunctionalInterface
public interface GetMessageFunction {
  String getMessage(String msg, LoggerType type, int spanIndex);
}
