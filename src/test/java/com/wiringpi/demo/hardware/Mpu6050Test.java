package com.wiringpi.demo.hardware;

import com.wiringpi.jni.WiringPiSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

class Mpu6050Test {
    private static final Logger logger = LoggerFactory.getLogger(Mpu6050Test.class);

    @BeforeAll
    public static void before() {
        WiringPiSetup.wiringPiSetup();
    }

    @Test
    public void testMpu6050() {
        Mpu6050 mpu6050 = new Mpu6050();
        new Thread(mpu6050).start();
        logger.debug("陀螺仪角速度偏移 : {} {} {}",
                num(mpu6050.getGyroAngularSpeedOffsetX()),
                num(mpu6050.getGyroAngularSpeedOffsetY()),
                num(mpu6050.getGyroAngularSpeedOffsetZ()));
        for (int i = 0; i < 1000; i++) {
            logger.debug("{}", mpu6050);
            logger.debug("陀螺仪-旋转角度-传感器值 : x={} , y={} , z={}",
                    num(mpu6050.getGyroAngularSpeedX()),
                    num(mpu6050.getGyroAngularSpeedY()),
                    num(mpu6050.getGyroAngularSpeedZ()));
            logger.debug("陀螺仪-旋转角度-计算结果 : x={} , y={} , z={}",
                    num(mpu6050.getFilteredAngleX()),
                    num(mpu6050.getFilteredAngleY()),
                    num(mpu6050.getFilteredAngleZ()));
            logger.debug("陀螺仪-加速度-传感器值 : x={} , y={} , z={}",
                    num(mpu6050.getAccelAccelerationX()),
                    num(mpu6050.getAccelAccelerationY()),
                    num(mpu6050.getAccelAccelerationZ()));
            logger.debug("陀螺仪-加速度-计算结果 : x={} , y={} , z={}",
                    num(mpu6050.getAccelAngleX()),
                    num(mpu6050.getAccelAngleY()),
                    num(mpu6050.getAccelAngleZ()));
            System.out.println();
            System.out.println();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        mpu6050.shutdown();
    }

    private double num(double num) {
        return BigDecimal.valueOf(num).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 测试振动
     *
     * @see <a href="https://github.com/everpast/Quadcopter_program-/blob/master/JAVA/Balance_control/src/main/java/usyd/Balance_control/App.java">代码参考第三方</a>
     */
    @Test
    public void testVibration() {
        Mpu6050 mpu6050 = new Mpu6050();
        new Thread(mpu6050).start();

        double error = 0;
        for (int i = 0; i < 100; i++) {
            double sysVibration = Math.pow(mpu6050.getFilteredAngleX(), 2) + Math.pow(mpu6050.getFilteredAngleY(), 2);
            logger.info("总振动为 {}", sysVibration);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            error += sysVibration;
        }
        error /= 100;
        logger.info("平均振动为 {}", error);
        mpu6050.shutdown();
    }
}