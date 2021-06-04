package com.trace.base.tool.mybatis.study.util.sql;

/**
 * 使用SQL类构造sql时预检异常
 *
 * @author ty
 */
public class SqlPreFlightException extends RuntimeException {
    static final String BLANK_ERROR = "不能为null或者空";

    SqlPreFlightException(String action, String error) {
        super(action + error);
    }

    SqlPreFlightException(String action, String error, Throwable throwable) {
        super(action + error, throwable);
    }
}
