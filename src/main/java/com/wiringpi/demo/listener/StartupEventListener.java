package com.wiringpi.demo.listener;

import com.wiringpi.jni.WiringPiSetup;
import com.wiringpi.pin.modes.WpiMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 应用程序上下文初始化事件
 *
 * @author HouKunLin
 * @date 2020/2/13 0013 1:34
 */
public class StartupEventListener implements ApplicationListener<ApplicationContextInitializedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StartupEventListener.class);

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        String property = event.getApplicationContext().getEnvironment().getProperty("pi.wiringpi", "0");
        String osName = System.getProperty("os.name");
        logger.debug("系统名称：{}", osName);
        if (osName.toLowerCase().contains("windows")) {
            logger.warn("处于Windows平台，无法加载 WiringPi 库，但是可以使用普通功能，当调用WiringPi的时候会报错");
            return;
        }
        int mode = Integer.parseInt(property);
        logger.info("正在启动 WiringPi");
        WiringPiSetup.setup(WpiMode.valueOf(mode));
        logger.info("启动 wiringPiSetup 完成");
    }
}
