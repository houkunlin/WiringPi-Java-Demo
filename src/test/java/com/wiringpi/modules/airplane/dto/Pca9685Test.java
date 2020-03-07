package com.wiringpi.modules.airplane.dto;

import com.wiringpi.demo.hardware.Pca9685;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Pca9685Test {
    private static final Logger logger = LoggerFactory.getLogger(Pca9685Test.class);

    @Test
    public void testPca9685() throws InterruptedException {
        Pca9685 pca9685 = new Pca9685(0x40);
        pca9685.setPWMFreq(50);
        int max = 2200;
        int min = 840;
        setPlan(pca9685, 0);

        logger.debug("等待几秒钟");
        Thread.sleep(10 * 1000);

        logger.debug("设置油门上限");
        setPlan(pca9685, max);
        Thread.sleep(5 * 1000);

        logger.debug("设置油门下限");
        setPlan(pca9685, min);
        Thread.sleep(10 * 1000);

        for (int i = min; i <= max; i += 10) {
            setPlan(pca9685, i);
            Thread.sleep(500);
        }
        Thread.sleep(5 * 1000);

        setPlan(pca9685, max);
        Thread.sleep(500);

        setPlan(pca9685, (max + min) / 2);
        Thread.sleep(500);

        setPlan(pca9685, min);
        logger.debug("结束");
    }

    private void setPlan(Pca9685 pca9685, int pulse) {
        pca9685.setServoPulse(0, pulse);
        pca9685.setServoPulse(1, pulse);
        pca9685.setServoPulse(2, pulse);
        pca9685.setServoPulse(3, pulse);
    }
}