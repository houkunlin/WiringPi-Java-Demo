package com.wiringpi.modules.airplane.dto;

import com.wiringpi.jni.WiringPiI2C;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 姿态
 *
 * @author HouKunLin
 * @date 2020/2/21 0021 16:35
 */
@Data
public class Posture {
    private static final Logger logger = LoggerFactory.getLogger(Posture.class);
    private WiringPiI2C piI2C;
    /**
     * 加速度X
     */
    private double accX;
    /**
     * 加速度Y
     */
    private double accY;
    /**
     * 加速度Z
     */
    private double accZ;
    /**
     * 旋转X
     */
    private double gyroX;
    /**
     * 旋转Y
     */
    private double gyroY;
    /**
     * 旋转Z
     */
    private double gyroZ;
    /**
     * 温度
     */
    private double temp;

    /**
     * 0x3B，加速度计的X轴分量ACC_X
     * 0x3D，加速度计的Y轴分量ACC_Y
     * 0x3F，加速度计的Z轴分量ACC_Z
     * <p>
     * 0x41，当前温度TEMP
     * <p>
     * 0x43，绕X轴旋转的角速度GYR_X
     * 0x45，绕Y轴旋转的角速度GYR_Y
     * 0x47，绕Z轴旋转的角速度GYR_Z
     */
    public Posture() {
        this.piI2C = new WiringPiI2C(0x68);
        this.piI2C.wiringPiI2CWriteReg8(0x6B, 0x00);
        logger.debug("set 0x6B={}", this.piI2C.wiringPiI2CReadReg8(0x6B));
    }

    public void refresh1() {
        // 绕X轴旋转的角速度GYR_X
        int x = piI2C.readWord2c(0x43);
        // 绕Y轴旋转的角速度GYR_Y
        int y = piI2C.readWord2c(0x45);
        // 绕Z轴旋转的角速度GYR_Z
        int z = piI2C.readWord2c(0x47);

        gyroX = x / 131.0;
        gyroY = y / 131.0;
        gyroZ = z / 131.0;
    }

    public Posture refresh2() {
        // 加速度计的X轴分量ACC_X
        int x = piI2C.readWord2c(0x3B);
        // 加速度计的Y轴分量ACC_Y
        int y = piI2C.readWord2c(0x3D);
        // 加速度计的Z轴分量ACC_Z
        int z = piI2C.readWord2c(0x3F);

        accX = x / 16384.0;
        accY = y / 16384.0;
        accZ = z / 16384.0;

        gyroX = calcXRotation(accX, accY, accZ);
        gyroY = calcYRotation(accX, accY, accZ);
        return this;
    }

    double dist(double a, double b) {
        return Math.sqrt((a * a) + (b * b));
    }

    double calcXRotation(double accX, double accY, double accZ) {
        double radians = Math.atan2(accY, dist(accX, accZ));
        return (radians * (180.0 / Math.PI));
    }

    double calcYRotation(double accX, double accY, double accZ) {
        double radians = Math.atan2(accX, dist(accY, accZ));
        return -(radians * (180.0 / Math.PI));
    }
}
