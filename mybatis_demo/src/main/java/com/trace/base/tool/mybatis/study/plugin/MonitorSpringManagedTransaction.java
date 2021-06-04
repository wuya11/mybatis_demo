package com.trace.base.tool.mybatis.study.plugin;

import org.mybatis.spring.transaction.SpringManagedTransaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 具备监控功能的spring事务管理器
 *
 * @author ty
 */
public class MonitorSpringManagedTransaction extends SpringManagedTransaction {

    public MonitorSpringManagedTransaction(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        long start = System.currentTimeMillis();
        Connection connection = super.getConnection();
        try {
            long end = System.currentTimeMillis();
            long used = end - start;
            // 获取数据库连接池超过500豪秒
            final long max = 500L;
            if (used >= max) {
                // todo 根据项目需求，调整对应的信息
//                SlowSqlLog slowSqlLog = new SlowSqlLog();
//                slowSqlLog.setTraceId("nid-3232122");
//                slowSqlLog.setMessage("获取数据库连接池超时500毫秒");
//                slowSqlLog.setType(SlowSqlEnum.CONNECTION_POOL);
//                slowSqlLog.setStart(LocalDateTimeUtil.formatMilliPlus8(start));
//                slowSqlLog.setEnd(LocalDateTimeUtil.formatMilliPlus8(end));
//                slowSqlLog.setUsed(used);
//                BaseLog<SlowSqlLog> baseLog = new BaseLog<>();
//                baseLog.setContext(slowSqlLog);
//                baseLog.setLevel(LevelEnum.WARNING.getLevel());
//                baseLog.setLevelName(LevelEnum.WARNING.getLevelName());
//                baseLog.setChannel(Channel.SYSTEM);
//                baseLog.setMessage("slowsql log");
//                baseLog.setDatetime(LocalDateTimeUtil.getMicroSecondFormattedNow());
                // todo 记录日志信息
            }
        } catch (Throwable th) {
            // ignore
        }
        return connection;
    }
}
