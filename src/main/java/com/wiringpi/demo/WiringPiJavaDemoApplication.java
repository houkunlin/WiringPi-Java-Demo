package com.wiringpi.demo;

import com.wiringpi.demo.listener.ShutdownEventListener;
import com.wiringpi.demo.listener.StartupEventListener;
import com.wiringpi.modules.airplane.Airplane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动程序
 *
 * @author HouKunLin
 * @date 2020/2/12 0012 20:51
 */
@EnableScheduling
@EnableAsync
@EnableCaching
@SpringBootApplication(scanBasePackageClasses = {WiringPiJavaDemoApplication.class, Airplane.class})
@ConfigurationPropertiesScan
public class WiringPiJavaDemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(WiringPiJavaDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WiringPiJavaDemoApplication.class);
        app.addListeners(
                // new CustomApplicationListener(),
                new StartupEventListener(),
                new ShutdownEventListener()
        );
        app.run(args);
    }
}
