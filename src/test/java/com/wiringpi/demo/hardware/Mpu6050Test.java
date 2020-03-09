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
        Mpu6050.AngularResult angularResult = mpu6050.getAngularResult();
        for (int i = 0; i < 1000; i++) {
            logger.debug("{}", mpu6050);
            Mpu6050.Gyro gyro = mpu6050.getGyro();
            Mpu6050.Acceleration acceleration = mpu6050.getAcceleration();
            logger.debug("陀螺仪-角速度-传感器值 : x={} , y={} , z={}",
                    num(gyro.getAngularSpeedX()),
                    num(gyro.getAngularSpeedY()),
                    num(gyro.getAngularSpeedZ()));
            logger.debug("陀螺仪-角速度-计算结果 : x={} , y={} , z={}",
                    num(gyro.getResultX()),
                    num(gyro.getResultY()),
                    num(gyro.getResultZ()));

            logger.debug("重力加速度计-传感器值 : x={} , y={} , z={}",
                    num(acceleration.getDivLbsX()),
                    num(acceleration.getDivLbsY()),
                    num(acceleration.getDivLbsZ()));
            logger.debug("重力加速度计-旋转角度计算结果 : x={} , y={} , z={}",
                    num(acceleration.getAbsoluteRotationX()),
                    num(acceleration.getAbsoluteRotationY()),
                    num(0));
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
            double sysVibration = Math.pow(mpu6050.getAngularResult().getAbsoluteAngleX(), 2) +
                    Math.pow(mpu6050.getAngularResult().getAbsoluteAngleY(), 2);
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