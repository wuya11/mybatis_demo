package com.trace.base.tool.mybatis.study.plugin;

import com.zaxxer.hikari.pool.ProxyStatement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.postgresql.jdbc.PgStatement;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

/**
 * 监控慢SQL插件
 *
 * @author wl
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class})
})
public class MonitorSlowSqlPlugin implements Interceptor {
    public static final String SLOW_SQL_ENABLE = "sql.slow.enable";
    private static boolean POSTGRESQL_DRIVER_AVAILABLE;
    private static boolean MYSQL_DRIVER_AVAILABLE;
    private static boolean HIKARICP_AVAILABLE;
    private static Field DELEGATE_FIELD;

    static {
        try {
            Class.forName("org.postgresql.jdbc.PgPreparedStatement");
            POSTGRESQL_DRIVER_AVAILABLE = true;
        } catch (ClassNotFoundException e) {
            // ignore
            POSTGRESQL_DRIVER_AVAILABLE = false;
        }
        try {
            Class.forName("com.mysql.jdbc.PreparedStatement");
            MYSQL_DRIVER_AVAILABLE = true;
        } catch (ClassNotFoundException e) {
            // ignore
            MYSQL_DRIVER_AVAILABLE = false;
        }
        try {
            Class.forName("com.zaxxer.hikari.pool.HikariProxyPreparedStatement");
            DELEGATE_FIELD = ProxyStatement.class.getDeclaredField("delegate");
            DELEGATE_FIELD.setAccessible(true);
            HIKARICP_AVAILABLE = true;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            // ignore
            HIKARICP_AVAILABLE = false;
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object obj = invocation.proceed();
            return obj;
        } finally {
            long end = System.currentTimeMillis();
            long used = end - start;
            // >= 1s
            final long max = 1000L;
            if (used >= max) {
                try {
                    Object target = invocation.getTarget();
                    String sql = "unknown";
                    if (target instanceof StatementHandler) {
                        sql = ((StatementHandler) target).getBoundSql().getSql();
                        //sql = actualSql(((StatementHandler) target).getBoundSql());
                    }
                    // 外部提前做一次猜测是否为预处理语句,只用instanceof PreparedStatement有可能没有?,这种情况不需要执行下面逻辑
                    boolean mightPreparedSql = sql.contains("?");
                    Object statementArg = invocation.getArgs()[0];
                    // 可能是预处理语句才处理
                    if (mightPreparedSql) {
                        // 这里还要区分是否为debug模式,debug模式下,生成的connection和statement都是被mybatis logger类代理
                        if (Proxy.isProxyClass(statementArg.getClass())) {
                            // 获取到真实被代理的statement
                            statementArg = ((PreparedStatementLogger) Proxy.getInvocationHandler(statementArg)).getPreparedStatement();
                        }
                        // 被HikariProxyPreparedStatement代理，通过反射才能获取到真实的PreparedStatement
                        if (HIKARICP_AVAILABLE && statementArg instanceof ProxyStatement) {
                            java.sql.PreparedStatement preparedStatement = (java.sql.PreparedStatement) DELEGATE_FIELD.get(statementArg);
                            // postgresql,前提是SQL为预处理语句,避免非预处理语句也执行了toString()造成拿到内存地址
                            if (POSTGRESQL_DRIVER_AVAILABLE && preparedStatement instanceof PgStatement) {
                                // 因为PgPreparedStatement是保护类,只能使用PgStatement转换,实际是执行子类的toString()
                                sql = preparedStatement.toString();
                            }
                            // mysql
                            else if (MYSQL_DRIVER_AVAILABLE && preparedStatement instanceof PreparedStatement) {
                                sql = preparedStatement.toString();
                            }
                            // SqlServer由于驱动限制无法获取到真实sql
                            else {
                                // 暂不支持
                            }
                        }
                        // sqlsever由于驱动限制无法获取到真实sql
                        else {
                          // 暂不支持
                        }
                    }
//                    SlowSqlLog slowSqlLog = new SlowSqlLog();
//                    slowSqlLog.setTraceId("idnum-0001");
//                    slowSqlLog.setType(SlowSqlEnum.DML);
//                    slowSqlLog.setMessage("执行DML[" + sql + "]超时1秒");
//                    slowSqlLog.setStart(LocalDateTimeUtil.formatMilliPlus8(start));
//                    slowSqlLog.setEnd(LocalDateTimeUtil.formatMilliPlus8(end));
//                    slowSqlLog.setUsed(used);
//                    BaseLog<SlowSqlLog> baseLog = new BaseLog<>();
//                    baseLog.setContext(slowSqlLog);
//                    baseLog.setLevel(LevelEnum.WARNING.getLevel());
//                    baseLog.setLevelName(LevelEnum.WARNING.getLevelName());
//                    baseLog.setChannel(Channel.SYSTEM);
//                    baseLog.setMessage("slowsql log");
//                    baseLog.setDatetime(LocalDateTimeUtil.getMicroSecondFormattedNow());
                    // todo 记录信息
                } catch (Throwable ex) {
                    // ignore
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
}

