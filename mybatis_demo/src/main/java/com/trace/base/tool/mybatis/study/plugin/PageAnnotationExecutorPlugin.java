package com.trace.base.tool.mybatis.study.plugin;

import com.trace.base.tool.annotation.Enhancer;
import com.trace.base.tool.mybatis.study.base.Page;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

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
 * 基于注解 Executor时，拦截
 *
 * @author wl
 * @date 2021-5-26
 */
@Intercepts(
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
)
public class PageAnnotationExecutorPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        BoundSql boundSql = getPageBoundSql(invocation);
        if (Objects.nonNull(boundSql)) {
            MappedStatement newMs = copyAndNewMS(mappedStatement, new SonOfSqlSource(boundSql));
            invocation.getArgs()[0] = newMs;
        }
        return invocation.proceed();
    }


    private BoundSql getPageBoundSql(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        BoundSql newBs = null;
        if (existEnhancer(mappedStatement)) {
            // 存在分页标识
            Page page = getPage(boundSql);
            if (Objects.nonNull(page)) {
                int total = getTotalSize(mappedStatement, boundSql);
                if (total <= 0) {
                    // 返回数量小于零，查询一个简单的sql,不去执行明细查询 【基于反射，重新设置boundSql】
                    String sql = "select * from (select 0 as id) as temp where  id>0";
                    newBs = new BoundSql(mappedStatement.getConfiguration(), sql, Collections.emptyList(), null);
                } else {
                    page.calculate(total);
                    boolean limitExist = boundSql.getSql().trim().toLowerCase().contains("limit");
                    if (!limitExist) {
                        String sql = boundSql.getSql() + " limit " + (page.getCurPage() - 1) * page.getPageSize() + ", " + page.getPageSize();
                        newBs = copyAndSetNewBoundSql(mappedStatement, boundSql, sql);
                    }
                }
            } else {
                throw new Exception("分页必须传入page参数");
            }
        }
        return newBs;
    }

    /***
     * 查看注解的自定义插件是否存在
     * @param mappedStatement 参数
     * @return 返回检查结果
     * @throws Throwable 抛出异常
     */
    private boolean existEnhancer(MappedStatement mappedStatement) throws Throwable {
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
                if (Objects.nonNull(enhancer) && enhancer.autoPageCount()) {
                    // 设置page
                    return true;
                }
            }
        }
        return false;
    }

    /***
     * 统计查询的总数量
     * @param mappedStatement 运行mp对象
     * @param boundSql sql对象
     * @return 查询的总数
     */
    private int getTotalSize(MappedStatement mappedStatement, BoundSql boundSql) {
        Connection connection = null;
        ResultSet rs = null;
        PreparedStatement countStatement = null;
        String countSql = getCountSql(boundSql.getSql());
        Map<String, Object> params = (Map<String, Object>) boundSql.getParameterObject();
        try {
            connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
            countStatement = connection.prepareStatement(countSql);
            BoundSql countBs = copyAndSetNewBoundSql(mappedStatement, boundSql, countSql);
            //当sql带有参数时，下面的这句话就是获取查询条件的参数
            DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, params, countBs);
            //经过set方法，就可以正确的执行sql语句
            parameterHandler.setParameters(countStatement);
            rs = countStatement.executeQuery();
            //当结果集中有值时，表示页面数量大于等于1
            if (rs.next()) {
                //根据业务需要对分页对象进行设置
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (Objects.nonNull(rs)) {
                    rs.close();
                }
                if (Objects.nonNull(countStatement)) {
                    countStatement.close();
                }
                if (Objects.nonNull(connection)) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /***
     * 构建一个新的sql对象
     * @param mappedStatement 运行的ms对象
     * @param boundSql sql对象
     * @param newSql 新生成的sql
     * @return 一个新的BoundSql()对象
     */
    private BoundSql copyAndSetNewBoundSql(MappedStatement mappedStatement, BoundSql boundSql, String newSql) {
        //根据新的sql构建一个全新的boundsql对象，并将原来的boundsql中的各属性复制过来
        BoundSql newBs = new BoundSql(mappedStatement.getConfiguration(), newSql
                , boundSql.getParameterMappings(), boundSql.getParameterObject());
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBs.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBs;
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

    /***
     * 复制一个新的MappedStatement
     * @param ms 历史对象
     * @param ss 新sql对象
     * @return 新的MappedStatement
     */
    private MappedStatement copyAndNewMS(MappedStatement ms, SqlSource ss) {
        //通过builder对象重新构建一个MappedStatement对象
        Builder builder = new Builder(ms.getConfiguration(), ms.getId(), ss, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();

    }

    /***
     * 构建一个SqlSource对象
     */
    class SonOfSqlSource implements SqlSource {
        private BoundSql boundSql;

        public SonOfSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object arg0) {
            return boundSql;
        }
    }
}
