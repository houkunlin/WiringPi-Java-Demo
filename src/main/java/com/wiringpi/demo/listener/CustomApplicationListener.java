package com.wiringpi.demo.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.*;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

public class CustomApplicationListener implements ApplicationListener {
    private final static Logger logger = LoggerFactory.getLogger(CustomApplicationListener.class);
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // 在这里可以监听到Spring Boot的生命周期
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            // 初始化环境变量
            logger.info("应用初始化环境变量");
        } else if (event instanceof ApplicationContextInitializedEvent) {
            // 应用程序上下文初始化事件
            logger.info("应用程序上下文初始化事件");
        } else if (event instanceof ApplicationPreparedEvent) {
            // 初始化完成
            logger.info("应用初始化完成");
        } else if (event instanceof ContextRefreshedEvent) {
            // 应用刷新
            logger.info("应用刷新");
        } else if (event instanceof ApplicationStartedEvent) {
            //应用启动，需要在代码动态添加监听器才可捕获
            logger.info("应用启动");
        } else if (event instanceof ApplicationReadyEvent) {
            // 应用已启动完成
            logger.info("应用启动完成，已经就绪");
        } else if (event instanceof ContextStartedEvent) {
            //应用启动，需要在代码动态添加监听器才可捕获
            logger.info("上下文启动");
        } else if (event instanceof ContextStoppedEvent) {
            // 应用停止
            logger.info("上下文停止");
        } else if (event instanceof ContextClosedEvent) {
            // 应用关闭
            logger.info("上下文关闭");
        } else if (event instanceof ApplicationFailedEvent) {
            // 应用程序失败事件
            logger.info("应用程序失败事件");
        } else {
            logger.info("其他事件：{}", event.getClass().getName());
        }
    }
}
