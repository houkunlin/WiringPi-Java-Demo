package com.wiringpi.modules.ggyro;

import com.wiringpi.jni.WiringPiI2C;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 陀螺仪数据
 *
 * @author HouKunLin
 * @date 2020/2/22 0022 19:18
 */
@Data
public class Ggyro {
    private static final Logger logger = LoggerFactory.getLogger(Ggyro.class);
    private WiringPiI2C piI2C;
    private int x;
    private int y;
    private int z;
    private double xScaled;
    private double yScaled;
    private double zScaled;
    private double xRotation;
    private double yRotation;

    private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#0.000");

    static {
        DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    public Ggyro() {
        this.piI2C = new WiringPiI2C(0x68);
        this.piI2C.wiringPiI2CWriteReg8(0x6B, 0x00);
        logger.debug("set 0x6B={}", this.piI2C.wiringPiI2CReadReg8(0x6B));
    }

    public Ggyro refresh1() {
        x = piI2C.readWord2c(0x43);
        y = piI2C.readWord2c(0x45);
        z = piI2C.readWord2c(0x47);

        xScaled = x / 131.0;
        yScaled = y / 131.0;
        zScaled = z / 131.0;

        xRotation = 0;
        yRotation = 0;
        return this;
    }

    public Ggyro refresh2() {
        x = piI2C.readWord2c(0x3B);
        y = piI2C.readWord2c(0x3D);
        z = piI2C.readWord2c(0x3F);

        xScaled = x / 16384.0;
        yScaled = y / 16384.0;
        zScaled = z / 16384.0;

        xRotation = calcXRotation(xScaled, yScaled, zScaled);
        yRotation = calcYRotation(xScaled, yScaled, zScaled);
        return this;
    }

    double dist(double a, double b) {
        return Math.sqrt((a * a) + (b * b));
    }

    double calcYRotation(double xScaled, double yScaled, double zScaled) {
        double radians = Math.atan2(xScaled, dist(yScaled, zScaled));
        return -(radians * (180.0 / Math.PI));
    }

    double calcXRotation(double xScaled, double yScaled, double zScaled) {
        double radians = Math.atan2(yScaled, dist(xScaled, zScaled));
        return (radians * (180.0 / Math.PI));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);

        map.put("xScaled", DECIMAL_FORMAT.format(xScaled));
        map.put("yScaled", DECIMAL_FORMAT.format(yScaled));
        map.put("zScaled", DECIMAL_FORMAT.format(zScaled));

        map.put("xRotation", DECIMAL_FORMAT.format(xRotation));
        map.put("yRotation", DECIMAL_FORMAT.format(yRotation));

        return map;
    }
}
