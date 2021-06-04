package com.trace.base.tool.mybatis.study;


import com.trace.base.tool.mybatis.study.plugin.MonitorSlowSqlPlugin;
import com.trace.base.tool.mybatis.study.plugin.PageAnnotationExecutorPlugin;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;


/**
 * 自定义的sqlSessionFactoryBean
 *
 * @author wl
 * @date 2021-3-9
 */
public class StudySqlSessionFactoryBean extends SqlSessionFactoryBean implements EnvironmentAware {
    private Interceptor[] plugins;
    public static Configuration CONFIGURATION;
    private boolean slowSqlEnabled = false;

    public StudySqlSessionFactoryBean() {
        this(null);
    }

    public StudySqlSessionFactoryBean(Configuration configuration) {
        super();
        if (configuration == null) {
            configuration = new Configuration();
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setAutoMappingBehavior(AutoMappingBehavior.FULL);
        }
        CONFIGURATION = configuration;
        setConfiguration(configuration);
    }

    @Override
    public void setPlugins(Interceptor[] plugins) {
        this.plugins = plugins;
    }

    /**
     * 真实执行设置插件,setPlugins只用于记录客户端自定义的plugin,便于后续拷贝
     */
    private void actualSetPlugins() {
        this.plugins = ArrayUtils.add(plugins == null ? new Interceptor[0] : plugins, new MonitorSlowSqlPlugin());
        this.plugins = ArrayUtils.add(plugins == null ? new Interceptor[0] : plugins, new PageAnnotationExecutorPlugin());
        super.setPlugins(plugins);
    }

    @Override
    public void setEnvironment(Environment environment) {
        slowSqlEnabled = environment.getProperty(MonitorSlowSqlPlugin.SLOW_SQL_ENABLE, boolean.class, true);
        actualSetPlugins();
    }
}
