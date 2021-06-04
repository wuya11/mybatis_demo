package com.trace.base.tool.mybatis.study.plugin;

import com.trace.base.tool.annotation.Enhancer;
import com.trace.base.tool.mybatis.study.base.Page;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 分页SQL插件
 * 基于注解实现 -prepare准备时拦截
 *
 * @author wl
 * @date 2021-5-26
 */
@Intercepts(
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
)
public class PageAnnotationPlugin implements Interceptor {


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        useMetaObject(invocation);
        return invocation.proceed();
    }


    private void useMetaObject(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        // 调用MetaObject 反射类处理
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        //分离代理对象链
        String markH = "h";
        String markTarget = "target";
        while (metaObject.hasGetter(markH)) {
            Object obj = metaObject.getValue(markH);
            metaObject = SystemMetaObject.forObject(obj);
        }
        while (metaObject.hasGetter(markTarget)) {
            Object obj = metaObject.getValue(markTarget);
            metaObject = SystemMetaObject.forObject(obj);
        }
        BoundSql boundSql = statementHandler.getBoundSql();
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (existEnhancer(mappedStatement)) {
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
            } else {
                throw new Exception("分页必须传入page参数");
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

    private boolean existEnhancer(MappedStatement mappedStatement) throws Throwable {
        // 不是select方法，直接返回
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return false;
        }
        //获取执行方法的位置
        String namespace = mappedStatement.getId();
        //获取mapper名称
        String className = namespace.substring(0, namespace.lastIndexOf("."));
        //获取方法名aClass = {Class@5974} "interface com.trace.base.tool.mapper.LifeLogMapper"… Navigate
        String methodName = namespace.substring(namespace.lastIndexOf(".") + 1);
        Class<?> aClass = Class.forName(className);
        for (Method method : aClass.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                // 暂不考虑重载
                Enhancer enhancer = method.getAnnotation(Enhancer.class);
                if (Objects.nonNull(enhancer)&&enhancer.autoPageCount()) {
                    // 设置page
                    return true;
                }
            }
        }
        return false;
    }

    /***
     * 获取分页的对象
     * @param boundSql 执行sql对象
     * @return 分页对象
     */
    private Page getPage(BoundSql boundSql) {
        Page page = null;
        Map<String, Object> parameterList = (Map<String, Object>) boundSql.getParameterObject();
        if (Objects.isNull(parameterList)) {
            return null;
        }
        for (Map.Entry<String, Object> entry : parameterList.entrySet()) {
            if (entry.getValue() instanceof Page) {
                page = (Page) entry.getValue();
                break;
            }
        }
        if (Objects.nonNull(page)) {
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
            pstmt = conn.prepareStatement(countSql);
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
