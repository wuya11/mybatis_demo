package com.trace.base.tool;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author wl
 */
@SpringBootApplication
@EnableAsync
public class TraceApplication {
    public static void main(String[] args) {
        System.out.println("Mybatis-测试微服务开始启动...........");
        System.setProperty("PROJECT_NAME", "customize-trace-A");
        new SpringApplicationBuilder(TraceApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
        System.out.println("Mybatis-测试微服务开始完成............");
    }
}
