package com.wiringpi.demo.listener;

import com.wiringpi.pin.IPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * 应用程序退出事件
 *
 * @author HouKunLin
 * @date 2020/2/13 0013 5:59
 */
public class ShutdownEventListener implements ApplicationListener<ContextClosedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownEventListener.class);

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        String osName = System.getProperty("os.name");
        logger.debug("系统名称：{}", osName);
        if (osName.toLowerCase().contains("windows")) {
            logger.warn("处于Windows平台，无法加载 WiringPi 库，但是可以使用普通功能，当调用WiringPi的时候会报错");
            return;
        }
        IPin.cleanPinStatus();
    }
}
