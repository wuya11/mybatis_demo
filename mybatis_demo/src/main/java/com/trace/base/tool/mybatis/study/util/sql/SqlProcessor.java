package com.trace.base.tool.mybatis.study.util.sql;

/**
 * sql扩展处理器，调用者可以获取sql对象
 *
 * @author ty
 */
@FunctionalInterface
public interface SqlProcessor {
    /**
     * 自定义sql处理
     *
     * @param sql sql对象
     */
    void process(SQL sql);
}
