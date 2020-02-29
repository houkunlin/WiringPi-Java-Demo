package com.wiringpi.demo.configure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.annotation.PostConstruct;

/**
 * 配置Bean
 *
 * @author HouKunLin
 * @date 2019/12/4 0004 0:02
 */
@Configuration
public class ApplicationBeanConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationBeanConfiguration.class);


    @PostConstruct
    public void postConstruct() {
    }
}
