package com.trace.base.tool.mybatis.study.plugin;

import com.trace.base.tool.mybatis.study.base.Page;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 分页SQL插件-基础版本
 * 在page对象中设置标识，不基于注解
 *
 * @author wl
 * @date 2021-5-26
 */
@Intercepts(
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
)
public class PagePlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        useMetaObjectPlus(invocation);
        return invocation.proceed();
    }

    private void useMetaObject(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        // 调用MetaObject 反射类处理
        //分离代理对象链
        while (metaObject.hasGetter("h")) {
            Object obj = metaObject.getValue("h");
            metaObject = SystemMetaObject.forObject(obj);
        }
        while (metaObject.hasGetter("target")) {
            Object obj = metaObject.getValue("target");
            metaObject = SystemMetaObject.forObject(obj);
        }
        BoundSql boundSql = statementHandler.getBoundSql();
        // 存在分页标识
        Page page = getPage(boundSql);
        if (Objects.nonNull(page)) {
            int total = getTotalSize(statementHandler, (Connection) invocation.getArgs()[0]);
            if (total <= 0) {
                // 返回数量小于零，查询一个简单的sql,不去执行明细查询 【基于反射，重新设置boundSql】
                String sql = "select * from (select 0 as id) as temp where  id>0";
                metaObject.setValue("delegate.boundSql.sql", sql);
                metaObject.setValue("delegate.boundSql.parameterMappings", Collections.emptyList());
                metaObject.setValue("delegate.boundSql.parameterObject", null);
            } else {
                page.calculate(total);
                boolean limitExist = boundSql.getSql().trim().toLowerCase().contains("limit");
                if (!limitExist) {
                    String sql = boundSql.getSql() + " limit " + (page.getCurPage() - 1) * page.getPageSize() + ", " + page.getPageSize();
                    metaObject.setValue("delegate.boundSql.sql", sql);
                }
            }
        }
    }

    private void useReflection(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        // 存在分页标识
        Page page = getPage(boundSql);
        if (Objects.nonNull(page)) {
            int total = getTotalSize(statementHandler, (Connection) invocation.getArgs()[0]);
            if (total <= 0) {
                // 返回数量小于零，查询一个简单的sql,不去执行明细查询 【基于反射，重新设置boundSql】
                Field fieldParameterMappings = BoundSql.class.getDeclaredField("parameterMappings");
                fieldParameterMappings.setAccessible(true);
                fieldParameterMappings.set(boundSql, Collections.emptyList());

                Field fieldSql = BoundSql.class.getDeclaredField("sql");
                fieldSql.setAccessible(true);
                String sql = "select * from (select 0 as id) as temp where  id>0";
                fieldSql.set(boundSql, sql);

                Field fieldParameterObject = BoundSql.class.getDeclaredField("parameterObject");
                fieldParameterObject.setAccessible(true);
                fieldParameterObject.set(boundSql, null);
            } else {
                page.calculate(total);
                // 设置分页的SQL代码
                boolean limitExist = boundSql.getSql().trim().toLowerCase().contains("limit");
                if (!limitExist) {
                    Field field = BoundSql.class.getDeclaredField("sql");
                    field.setAccessible(true);
                    field.set(boundSql, boundSql.getSql() + " limit " + (page.getCurPage() - 1) * page.getPageSize() + ", " + page.getPageSize());
                }
            }
        }
    }

    private void useMetaObjectPlus(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        // 存在分页标识
        Page page = getPage(boundSql);
        if (Objects.nonNull(page)) {
            int total = getTotalSize(statementHandler, (Connection) invocation.getArgs()[0]);
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            if (total <= 0) {
                // 返回数量小于零，查询一个简单的sql,不去执行明细查询 【基于反射，重新设置boundSql】
                String sql = "select * from (select 0 as id) as temp where  id>0";
                metaObject.setValue("sql", sql);
                metaObject.setValue("parameterMappings", Collections.emptyList());
                metaObject.setValue("parameterObject", null);
            } else {
                page.calculate(total);
                boolean limitExist = boundSql.getSql().trim().toLowerCase().contains("limit");
                if (!limitExist) {
                    String sql = boundSql.getSql() + " limit " + (page.getCurPage() - 1) * page.getPageSize() + ", " + page.getPageSize();
                    metaObject.setValue("sql", sql);
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /***
     * 获取分页的对象
     * @param boundSql 执行sql对象
     * @return 分页对象
     */
    private Page getPage(BoundSql boundSql) {
        Object obj = boundSql.getParameterObject();
        if (Objects.isNull(obj)) {
            return null;
        }
        Page page = null;
        if (obj instanceof Page) {
            page = (Page) obj;
        } else if (obj instanceof Map) {
            // 如果Dao中有多个参数，则分页的注解参数名必须是page
            try {
                page = (Page) ((Map) obj).get("page");
            } catch (Exception e) {
                return null;
            }
        }
        // 不存在分页对象，则忽略下面的分页逻辑
        if (Objects.nonNull(page) && page.isAutoCount()) {
            return page;
        }
        return null;
    }

    /**
     * 查询总记录数
     *
     * @param statementHandler mybatis sql 对象
     * @param conn             链接信息
     */
    private int getTotalSize(StatementHandler statementHandler, Connection conn) {
        ParameterHandler parameterHandler = statementHandler.getParameterHandler();
        String countSql = getCountSql(statementHandler.getBoundSql().getSql());
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = (PreparedStatement) conn.prepareStatement(countSql);
            parameterHandler.setParameters(pstmt);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                // 设置总记录数
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /***
     * 获取统计sql
     * @param originalSql 原始sql
     * @return 返回统计加工的sql
     */
    private String getCountSql(String originalSql) {
        originalSql = originalSql.trim().toLowerCase();
        // 判断是否存在 limit 标识
        boolean limitExist = originalSql.contains("limit");
        if (limitExist) {
            originalSql = originalSql.substring(0, originalSql.indexOf("limit"));
        }
        boolean distinctExist = originalSql.contains("distinct");
        boolean groupExist = originalSql.contains("group by");
        if (distinctExist || groupExist) {
            return "select count(1) from (" + originalSql + ") temp_count";
        }
        // 去掉 order by
        boolean orderExist = originalSql.contains("order by");
        if (orderExist) {
            originalSql = originalSql.substring(0, originalSql.indexOf("order by"));
        }
        // todo   left join还可以考虑优化
        int indexFrom = originalSql.indexOf("from");
        return "select count(*)  " + originalSql.substring(indexFrom);
    }
}
