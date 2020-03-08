package com.wiringpi.demo.hardware;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wiringpi.jni.WiringPiI2C;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MPU6050 陀螺仪传感器
 *
 * @author HouKunLin
 * @date 2020/3/8 0008 13:51
 * @see <a href="https://github.com/everpast/Quadcopter_program-/blob/master/JAVA/Pathfinder/src/main/java/de/buschbaum/java/pathfinder/device/mpu6050/Mpu6050Controller.java">代码参考第三方</a>
 */
@Getter
@ToString
public class Mpu6050 implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Mpu6050.class);
    private final boolean isDebug = logger.isDebugEnabled();

    @JsonIgnore
    private WiringPiI2C piI2C;
    /**
     * 陀螺旋转角传感器值
     */
    private double gyroAngularSpeedX;
    /**
     * 陀螺旋转角传感器值
     */
    private double gyroAngularSpeedY;
    /**
     * 陀螺旋转角传感器值
     */
    private double gyroAngularSpeedZ;
    /**
     * 陀螺旋转角计算结果
     */
    private double gyroAngleX = 0;
    /**
     * 陀螺旋转角计算结果
     */
    private double gyroAngleY = 0;
    /**
     * 陀螺旋转角计算结果
     */
    private double gyroAngleZ = 0;

    /**
     * 陀螺仪角速度偏移，对象初始化时会初始化该参数
     */
    private double gyroAngularSpeedOffsetX;
    /**
     * 陀螺仪角速度偏移，对象初始化时会初始化该参数
     */
    private double gyroAngularSpeedOffsetY;
    /**
     * 陀螺仪角速度偏移，对象初始化时会初始化该参数
     */
    private double gyroAngularSpeedOffsetZ;

    /**
     * 过滤角度，绝对角度，旋转角度计算结果
     */
    private double filteredAngleX = 0;
    private double filteredAngleX1 = 0;
    /**
     * 过滤角度，绝对角度，旋转角度计算结果
     */
    private double filteredAngleY = 0;
    private double filteredAngleY1 = 0;
    /**
     * 过滤角度，绝对角度，旋转角度计算结果
     */
    private double filteredAngleZ = 0;

    /**
     * 加速度传感器值
     */
    private double accelAccelerationX;
    /**
     * 加速度传感器值
     */
    private double accelAccelerationY;
    /**
     * 加速度传感器值
     */
    private double accelAccelerationZ;
    /**
     * 加速度计算结果
     */
    private double accelAngleX = 0;
    /**
     * 加速度计算结果
     */
    private double accelAngleY = 0;
    /**
     * 加速度计算结果
     */
    private double accelAngleZ = 0;
    private double rotationX = 0;
    private double rotationY = 0;

    public double initialAccAngleX;
    public double initialAccAngleY;

    private static final double RADIAN_TO_DEGREE = 180. / Math.PI;
    private final static double ACCEL_LSB_SENSITIVITY = 16384;
    private final static double GYRO_LSB_SENSITIVITY = 131;
    /**
     * if indicator <0    decrease
     */
    private final static double OFFSET_ACCEL_ANGLE_X = -12.45;
    /**
     * if indicator >0    increase
     */
    private final static double OFFSET_ACCEL_ANGLE_Y = 5.77;

    public long lastUpdateTime = 0;

    private boolean run;

    /**
     * 默认地址应该是 0x68
     */
    public Mpu6050() {
        this(0x68);
    }

    /**
     * 默认地址应该是 0x68
     *
     * @param address 设备地址
     */
    public Mpu6050(int address) {
        this.piI2C = new WiringPiI2C(address);
        logger.info("I2C 总线载入 MPU6050 设备");
        initialize();
        logger.info("I2C 总线 MPU6050 设备初始化成功");
    }

    public void initialize() {
        // Waking up device
        write(0x6B, 0x00);
        // Configuring sample rate
        write(0x19, 0x00);
        // Setting global config (digital low pass filter)
        write(0x1A, 0x01);
        // Configuring gyroscope
        write(0x1B, 0x00);
        // Configuring accelerometer
        write(0x1C, 0x00);
        // Configuring interrupts
        write(0x38, 0x00);
        // Configuring low power operations
        write(0x6C, 0x00);
        calibrateSensors();
    }

    private void write(int reg, int data) {
        data = data & 0xFF;
        piI2C.wiringPiI2CWriteReg8(reg, data);
        if (piI2C.wiringPiI2CReadReg8(reg) != data) {
            logger.error("向 0x{} 设备的 0x{} 寄存器写入数据 0x{} 失败",
                    Integer.toHexString(piI2C.getDevId()),
                    Integer.toHexString(reg),
                    Integer.toHexString(data));
        }
    }

    public void calibrateSensors() {
        logger.info("校准将在5秒钟内开始（不要移动传感器）。");

        int nbReadings = 100;


        initialAccAngleX = 0.0;

        initialAccAngleY = 0.0;

        // Gyroscope offsets
        gyroAngularSpeedOffsetX = 0.0;
        gyroAngularSpeedOffsetY = 0.0;
        gyroAngularSpeedOffsetZ = 0.0;
        for (int i = 0; i < nbReadings; i++) {
            double[] angularSpeeds = readScaledGyroscopeValues();
            gyroAngularSpeedOffsetX += angularSpeeds[0];
            gyroAngularSpeedOffsetY += angularSpeeds[1];
            gyroAngularSpeedOffsetZ += angularSpeeds[2];
            double[] accelerations = readScaledAccelerometerValues();
            accelAccelerationX = accelerations[0];
            accelAccelerationY = accelerations[1];
            accelAccelerationZ = accelerations[2];
            accelAngleX = getAccelXAngle(accelAccelerationX, accelAccelerationY, accelAccelerationZ);
            accelAngleY = getAccelYAngle(accelAccelerationX, accelAccelerationY, accelAccelerationZ);
            initialAccAngleX += accelAngleX;
            initialAccAngleY += accelAngleY;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        gyroAngularSpeedOffsetX /= nbReadings;
        gyroAngularSpeedOffsetY /= nbReadings;
        gyroAngularSpeedOffsetZ /= nbReadings;

        initialAccAngleX /= nbReadings;
        initialAccAngleY /= nbReadings;

        // 绝对角度
        filteredAngleX = initialAccAngleX - OFFSET_ACCEL_ANGLE_X;
        filteredAngleY = initialAccAngleY - OFFSET_ACCEL_ANGLE_Y;
        // 测量零
        // filteredAngleX=0;
        // filteredAngleY=0;

        logger.info("校准结束");
    }


    public void updateValues() {
        // 加速度计
        double[] accelerations = readScaledAccelerometerValues();
        // 加速度传感器原始值
        accelAccelerationX = accelerations[0];
        accelAccelerationY = accelerations[1];
        accelAccelerationZ = accelerations[2];

        // 加速度计算结果
        accelAngleX = getAccelXAngle(accelAccelerationX, accelAccelerationY, accelAccelerationZ);
        accelAngleY = getAccelYAngle(accelAccelerationX, accelAccelerationY, accelAccelerationZ);
        accelAngleZ = getAccelZAngle();

        rotationX = get_x_rotation(accelAccelerationX, accelAccelerationY, accelAccelerationZ);
        rotationY = get_y_rotation(accelAccelerationX, accelAccelerationY, accelAccelerationZ);

        // 陀螺仪旋转角度
        double[] angularSpeeds = readScaledGyroscopeValues();
        // 旋转角传感器原始值
        gyroAngularSpeedX = angularSpeeds[0] - gyroAngularSpeedOffsetX;
        gyroAngularSpeedY = angularSpeeds[1] - gyroAngularSpeedOffsetY;
        gyroAngularSpeedZ = angularSpeeds[2] - gyroAngularSpeedOffsetZ;
        // angular speed * time = angle
        double dt = Math.abs(System.currentTimeMillis() - lastUpdateTime) / 1000.0;
        ; // s
        double deltaGyroAngleX = gyroAngularSpeedX * dt;
        double deltaGyroAngleY = gyroAngularSpeedY * dt;
        double deltaGyroAngleZ = gyroAngularSpeedZ * dt;

        lastUpdateTime = System.currentTimeMillis();
        // logger.debug("陀螺仪角度x速度为 {}", gyroAngularSpeedX);
        // logger.debug("陀螺仪角度y速度为 {}", gyroAngularSpeedY);
        // logger.debug("陀螺仪加速度x速度为 {}", accelAngleX);
        // logger.debug("陀螺仪加速度y速度为 {}", accelAngleY);
        gyroAngleX = deltaGyroAngleX;
        gyroAngleY = deltaGyroAngleY;
        gyroAngleZ = deltaGyroAngleZ;

        // Complementary Filter
        double alpha = 0.96;
        // 旋转角绝对角度
        filteredAngleX = alpha * (filteredAngleX + deltaGyroAngleX) + (1. - alpha) * (accelAngleX - OFFSET_ACCEL_ANGLE_X);
        filteredAngleY = alpha * (filteredAngleY + deltaGyroAngleY) + (1. - alpha) * (accelAngleY - OFFSET_ACCEL_ANGLE_Y);
        ////////////measure zero/////
        filteredAngleX1 = alpha * (filteredAngleX + deltaGyroAngleX) + (1. - alpha) * (accelAngleX - initialAccAngleX);
        filteredAngleY1 = alpha * (filteredAngleY + deltaGyroAngleY) + (1. - alpha) * (accelAngleY - initialAccAngleY);
        filteredAngleZ = filteredAngleZ + deltaGyroAngleZ;
    }

    /**
     * 获取加速度传感器值
     *
     * @return [X, Y, Z]
     */
    public double[] readScaledAccelerometerValues() {
        int accelX = readWord2C(0x3B);
        double accelX1 = accelX / ACCEL_LSB_SENSITIVITY;
        int accelY = readWord2C(0x3D);
        double accelY1 = accelY / ACCEL_LSB_SENSITIVITY;
        int accelZ = readWord2C(0x3F);
        double accelZ1 = accelZ / ACCEL_LSB_SENSITIVITY;

        return new double[]{accelX1, accelY1, -accelZ1};
    }

    private double getAccelXAngle(double x, double y, double z) {
        double accTotalVector = Math.sqrt(x * x + y * y + z * z);
        double angleRollAcc = Math.asin(x / accTotalVector) * -57.296;
        // logger.debug("角倾角加速度 angleRollAcc={}", angleRollAcc);
        return angleRollAcc;
    }

    private double getAccelYAngle(double x, double y, double z) {
        double accTotalVector = Math.sqrt(x * x + y * y + z * z);
        double anglePitchAcc = Math.asin(y / accTotalVector) * 57.296;
        // logger.debug("角距加速度 anglePitchAcc={}", anglePitchAcc);
        return anglePitchAcc;
    }

    private double getAccelZAngle() {
        return accelAngleZ;
    }

    double dist(double a, double b) {
        return Math.sqrt((a * a) + (b * b));
    }

    double get_x_rotation(double x, double y, double z) {
        double radians;
        radians = Math.atan2(y, dist(x, z));
        return (radians * (180.0 / Math.PI));
    }

    double get_y_rotation(double x, double y, double z) {
        double radians;
        radians = Math.atan2(x, dist(y, z));
        return -(radians * (180.0 / Math.PI));
    }

    /**
     * 获取陀螺仪旋转角度传感器旋转值
     *
     * @return [X, Y, Z]
     */
    public double[] readScaledGyroscopeValues() {
        double gyroX = readWord2C(0x43);
        gyroX /= GYRO_LSB_SENSITIVITY;
        double gyroY = readWord2C(0x45);
        gyroY /= GYRO_LSB_SENSITIVITY;
        double gyroZ = readWord2C(0x47);
        gyroZ /= GYRO_LSB_SENSITIVITY;

        return new double[]{gyroX, gyroY, gyroZ};
    }

    /**
     * 读取两个寄存器地址的值，并转换为整型
     *
     * @param address 传感器地址
     * @return 寄存器地址的值
     */
    public int readWord2C(int address) {
        int value = piI2C.wiringPiI2CReadReg8(address);
        value = value << 8;
        value += piI2C.wiringPiI2CReadReg8(address + 1);

        if (value >= 0x8000) {
            value = -(65536 - value);
        }
        return value;
    }

    @Override
    public void run() {
        lastUpdateTime = System.currentTimeMillis();
        run = true;
        while (run) {
            updateValues();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("睡眠错误中断");
            }
        }
    }

    public void shutdown() {
        this.run = false;
    }
}
