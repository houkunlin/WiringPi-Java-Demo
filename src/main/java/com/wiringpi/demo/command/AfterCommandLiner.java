package com.wiringpi.demo.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 启动后操作
 *
 * @author HouKunLin
 * @date 2020/2/13 0013 15:48
 */
@Component
public class AfterCommandLiner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(AfterCommandLiner.class);

    @Override
    public void run(String... args) throws Exception {
    }
}
