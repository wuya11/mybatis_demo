package com.trace.base.tool.mybatis.study.base;

/**
 * sql慢日志类型
 *
 * @author ty
 */
public enum SlowSqlEnum {
    /**
     * 获取数据库连接池
     */
    CONNECTION_POOL,
    /**
     * 执行DML
     */
    DML
}
