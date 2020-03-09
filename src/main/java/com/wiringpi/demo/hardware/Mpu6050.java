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
    private Gyro gyro = new Gyro();
    private Acceleration acceleration = new Acceleration();
    private Calculation calculation = new Calculation();
    private Temperature temperature = new Temperature();

    private final static double ACCEL_LSB_SENSITIVITY = 16384.0;
    private final static double GYRO_LSB_SENSITIVITY = 131.0;
    private final static double TEMP_LSB_SENSITIVITY = 340.0;
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
        // 电源管理，典型值：0x00(正常启用)
        write(0x6B, 0x00);
        // 陀螺仪输出率的分频，典型值0x07,(1kHz)
        write(0x19, 0x07);
        // 低通滤波频率,一般0x01~0x05,数值越大带宽越小延时越长
        write(0x1A, 0x01);
        // 配置陀螺仪，陀螺仪自检及测量范围，一般0x18(不自检，量程2000度/s)，其他量程分别为250度/s，500度/s，1000度/s，对应的值分别为 0x00，0x08，0x10。
        // 陀螺仪最高分辨率 131 LSB/( º/s)
        write(0x1B, 0x00);
        // 配置加速度计，加速计自检、测量范围，一般不自检，四种量程 2g，4g，8g，16g，对应的值分别为 0x00，0x08，0x10，0x18。
        // 加速度最高分辨率 16384 LSB/g
        write(0x1C, 0x00);
        // 配置中断
        write(0x38, 0x00);
        // 配置低功耗操作
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
        logger.info("校准将在10秒钟内开始（不要移动传感器）。");

        int nbReadings = 100;

        // 初始角度加速度
        double initialAccelerationAngleX = 0.0;
        double initialAccelerationAngleY = 0.0;

        // 陀螺仪偏移
        double gyroAngularSpeedOffsetX = 0.0;
        double gyroAngularSpeedOffsetY = 0.0;
        double gyroAngularSpeedOffsetZ = 0.0;
        for (int i = 0; i < nbReadings; i++) {
            gyroAngularSpeedOffsetX += readWord2C(0x43) / GYRO_LSB_SENSITIVITY;
            gyroAngularSpeedOffsetY += readWord2C(0x45) / GYRO_LSB_SENSITIVITY;
            gyroAngularSpeedOffsetZ += readWord2C(0x47) / GYRO_LSB_SENSITIVITY;

            double accelerationX = readWord2C(0x3B) / ACCEL_LSB_SENSITIVITY;
            double accelerationY = readWord2C(0x3D) / ACCEL_LSB_SENSITIVITY;
            double accelerationZ = readWord2C(0x3F) / ACCEL_LSB_SENSITIVITY;
            initialAccelerationAngleX += Acceleration.getAccelerationAngleX(accelerationX, accelerationY, accelerationZ);
            initialAccelerationAngleY += Acceleration.getAccelerationAngleY(accelerationX, accelerationY, accelerationZ);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        gyroAngularSpeedOffsetX /= nbReadings;
        gyroAngularSpeedOffsetY /= nbReadings;
        gyroAngularSpeedOffsetZ /= nbReadings;

        initialAccelerationAngleX /= nbReadings;
        initialAccelerationAngleY /= nbReadings;

        // 绝对角度
        double filteredAngleX = initialAccelerationAngleX - OFFSET_ACCEL_ANGLE_X;
        double filteredAngleY = initialAccelerationAngleY - OFFSET_ACCEL_ANGLE_Y;

        gyro.setOffset(gyroAngularSpeedOffsetX, gyroAngularSpeedOffsetY, gyroAngularSpeedOffsetZ);
        acceleration.setOffset(initialAccelerationAngleX, initialAccelerationAngleY);

        calculation.init(
                gyroAngularSpeedOffsetX, gyroAngularSpeedOffsetY, gyroAngularSpeedOffsetZ,
                initialAccelerationAngleX, initialAccelerationAngleY,
                filteredAngleX, filteredAngleY);

        logger.info("校准结束");
    }


    public void updateValues() {
        // 加速度计
        acceleration.refresh(this);
        // 温度计
        temperature.refresh(this);
        // 陀螺仪旋转角度
        gyro.refresh(this, Math.abs(System.currentTimeMillis() - lastUpdateTime) / 1000.0);
        calculation.refresh(gyro, acceleration);

        lastUpdateTime = System.currentTimeMillis();
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
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("睡眠错误中断");
            }
        }
    }

    public void shutdown() {
        this.run = false;
    }

    /**
     * 陀螺仪（旋转角度计算）
     */
    @Data
    @ToString
    public static class Gyro {
        /**
         * 静止状态下校准得到的偏移平均值
         */
        private double offsetX;
        /**
         * 静止状态下校准得到的偏移平均值
         */
        private double offsetY;
        /**
         * 静止状态下校准得到的偏移平均值
         */
        private double offsetZ;
        /**
         * 除去LBS，得到角速度值，绝对角速度
         */
        private double gyroX;
        /**
         * 除去LBS，得到角速度值，绝对角速度
         */
        private double gyroY;
        /**
         * 除去LBS，得到角速度值，绝对角速度
         */
        private double gyroZ;
        /**
         * 角速度
         */
        private double angularSpeedX;
        /**
         * 角速度
         */
        private double angularSpeedY;
        /**
         * 角速度
         */
        private double angularSpeedZ;
        /**
         * 旋转角度
         */
        private double angleX;
        /**
         * 旋转角度
         */
        private double angleY;
        /**
         * 旋转角度
         */
        private double angleZ;

        public void refresh(Mpu6050 mpu6050, double longtime) {
            // 原始数据，该数据不具有展示价值
            double rawX = mpu6050.readWord2C(0x43);
            double rawY = mpu6050.readWord2C(0x45);
            double rawZ = mpu6050.readWord2C(0x47);

            // 除去LBS，得到角速度值，绝对角速度
            gyroX = rawX / GYRO_LSB_SENSITIVITY;
            gyroY = rawY / GYRO_LSB_SENSITIVITY;
            gyroZ = rawZ / GYRO_LSB_SENSITIVITY;

            // 相对角速度，计算实际的角速度，因为角速度有一定波动，需要记录静止状态时的波动数，然后以当前的 - 静止的 = 实际的角速度值
            angularSpeedX = gyroX - mpu6050.calculation.gyroAngularSpeedOffsetX;
            angularSpeedY = gyroY - mpu6050.calculation.gyroAngularSpeedOffsetY;
            angularSpeedZ = gyroZ - mpu6050.calculation.gyroAngularSpeedOffsetZ;

            // 计算这段时间内的旋转角度值 = 角度的速度 * time
            angleX = angularSpeedX * longtime;
            angleY = angularSpeedY * longtime;
            angleZ = angularSpeedZ * longtime;
        }

        public void setOffset(double offsetX, double offsetY, double offsetZ) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
    }

    /**
     * 加速度计算
     */
    @Getter
    @ToString
    public static class Acceleration {
        private static final double RADIAN_TO_DEGREE = 180.0 / Math.PI;
        /**
         * 静止状态下校准得到的偏移平均值
         */
        private double offsetX;
        /**
         * 静止状态下校准得到的偏移平均值
         */
        private double offsetY;
        /**
         * 除去LBS值，得到该方向的重力加速度，单位g
         */
        private double accelerationX;
        /**
         * 除去LBS值，得到该方向的重力加速度，单位g
         */
        private double accelerationY;
        /**
         * 除去LBS值，得到该方向的重力加速度，单位g
         */
        private double accelerationZ;
        /**
         * 通过重力加速度计算出旋转角度
         */
        private double rotationX;
        /**
         * 通过重力加速度计算出旋转角度
         */
        private double rotationY;

        public void refresh(Mpu6050 mpu6050) {
            // 原始数据，该数据不具有展示价值
            double rawX = mpu6050.readWord2C(0x3B);
            double rawY = mpu6050.readWord2C(0x3D);
            double rawZ = mpu6050.readWord2C(0x3F);

            // 除去LBS，得到该方向的重力加速度，单位g，绝对加速度值
            accelerationX = rawX / ACCEL_LSB_SENSITIVITY;
            accelerationY = rawY / ACCEL_LSB_SENSITIVITY;
            accelerationZ = rawZ / ACCEL_LSB_SENSITIVITY;

            // 方式一：计算旋转角度，该方式已被废弃，并且该方式的计算结果与方式而一致
            // angleX = getAccelerationAngleX(accelerationX, accelerationY, accelerationZ);
            // angleY = getAccelerationAngleY(accelerationX, accelerationY, accelerationZ);

            // 通过重力加速度计算出旋转角度
            rotationX = getRotationX(accelerationX, accelerationY, accelerationZ);
            rotationY = getRotationY(accelerationX, accelerationY, accelerationZ);
        }

        public void setOffset(double offsetX, double offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        @Deprecated
        public static double getAccelerationAngleX(double x, double y, double z) {
            return -(Math.asin(y / dist(x, y, z)) * RADIAN_TO_DEGREE);
        }

        @Deprecated
        public static double getAccelerationAngleY(double x, double y, double z) {
            return -(Math.asin(x / dist(x, y, z)) * RADIAN_TO_DEGREE);
        }

        public static double getRotationX(double x, double y, double z) {
            return -(Math.atan2(y, dist(x, z)) * RADIAN_TO_DEGREE);
        }

        public static double getRotationY(double x, double y, double z) {
            return -(Math.atan2(x, dist(y, z)) * RADIAN_TO_DEGREE);
        }

        public static double dist(double... nums) {
            double sum = 0;
            for (double num : nums) {
                sum += num * num;
            }
            return Math.sqrt(sum);
        }
    }

    /**
     * 计算数据
     */
    @Getter
    @ToString
    public static class Calculation {
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
         * 初始加速度角度
         */
        private double initialAccelerationAngleX;
        /**
         * 初始加速度角度
         */
        private double initialAccelerationAngleY;

        /**
         * 过滤角度，绝对角度，旋转角度计算结果
         */
        private double filteredAngleX;
        /**
         * 过滤角度，绝对角度，旋转角度计算结果
         */
        private double filteredAngleY;
        /**
         * 过滤角度，绝对角度，旋转角度计算结果
         */
        private double filteredAngleZ;

        private double filteredAngleX1;
        private double filteredAngleY1;

        /**
         * 初始化值
         *
         * @param gyroAngularSpeedOffsetX   陀螺仪角速度偏移
         * @param gyroAngularSpeedOffsetY   陀螺仪角速度偏移
         * @param gyroAngularSpeedOffsetZ   陀螺仪角速度偏移
         * @param initialAccelerationAngleX 初始加速度角度
         * @param initialAccelerationAngleY 初始加速度角度
         * @param filteredAngleX            过滤角度
         * @param filteredAngleY            过滤角度
         */
        public void init(double gyroAngularSpeedOffsetX, double gyroAngularSpeedOffsetY, double gyroAngularSpeedOffsetZ,
                         double initialAccelerationAngleX, double initialAccelerationAngleY,
                         double filteredAngleX, double filteredAngleY) {
            this.gyroAngularSpeedOffsetX = gyroAngularSpeedOffsetX;
            this.gyroAngularSpeedOffsetY = gyroAngularSpeedOffsetY;
            this.gyroAngularSpeedOffsetZ = gyroAngularSpeedOffsetZ;
            this.initialAccelerationAngleX = initialAccelerationAngleX;
            this.initialAccelerationAngleY = initialAccelerationAngleY;
            this.filteredAngleX = filteredAngleX;
            this.filteredAngleY = filteredAngleY;
            this.filteredAngleZ = 0;
        }

        /**
         * 刷新计算结果
         *
         * @param gyro         陀螺仪
         * @param acceleration 加速度
         */
        public void refresh(Gyro gyro, Acceleration acceleration) {
            // 互补过滤器
            double alpha = 0.96;
            // 旋转角绝对角度
            filteredAngleX = alpha * (filteredAngleX + gyro.getAngleX()) + (1.0 - alpha) * (acceleration.getRotationX() - OFFSET_ACCEL_ANGLE_X);
            filteredAngleY = alpha * (filteredAngleY + gyro.getAngleY()) + (1.0 - alpha) * (acceleration.getRotationY() - OFFSET_ACCEL_ANGLE_Y);
            // 测量零
            filteredAngleX1 = alpha * (filteredAngleX + gyro.getAngleX()) + (1.0 - alpha) * (acceleration.getRotationX() - initialAccelerationAngleX);
            filteredAngleY1 = alpha * (filteredAngleY + gyro.getAngleY()) + (1.0 - alpha) * (acceleration.getRotationY() - initialAccelerationAngleY);
            filteredAngleZ = filteredAngleZ + gyro.getAngleZ();
        }
    }

    /**
     * 温度传感器值
     */
    @Getter
    @ToString
    public static class Temperature {
        /**
         * 计算结果
         */
        private double temp;

        public void refresh(Mpu6050 mpu6050) {
            // 温度原始数据，该数据不具有展示价值
            double raw = mpu6050.readWord2C(0x41);

            // 除去LBS，该数据不具有展示价值
            double lbsValue = raw / TEMP_LSB_SENSITIVITY;

            // 计算温度结果（实际上该温度为芯片的温度，与实际温度有一定的差别）
            temp = lbsValue + 36.53;
        }
    }

}
