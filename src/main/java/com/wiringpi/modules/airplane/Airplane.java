package com.wiringpi.modules.airplane;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wiringpi.demo.hardware.Mpu6050;
import com.wiringpi.modules.airplane.dto.DirectionDTO;
import com.wiringpi.modules.airplane.dto.Gps;
import com.wiringpi.modules.airplane.dto.Motor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * 飞机
 *
 * @author HouKunLin
 * @date 2020/2/21 0021 16:33
 */
@Data
public class Airplane implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Airplane.class);
    /**
     * GPS 坐标
     */
    private Gps gps = new Gps();
    /**
     * 姿态数据
     */
    private Mpu6050 mpu6050 = new Mpu6050();
    /**
     * 方向数据
     */
    private DirectionDTO direction = new DirectionDTO();
    private AtomicInteger speed;
    /**
     * 电机
     */
    private Motor motor;

    private boolean run;
    /**
     * 0 最大油门 1最小油门
     */
    private boolean[] motorRun = new boolean[]{false, false};

    private Double[] posture = new Double[]{0.0, 0.0, 0.0, 0.0, 0.0};

    @JsonIgnore
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new CustomizableThreadFactory("Airplane-Thread-"));

    /**
     * 默认油门量，维持悬停的油门量
     */
    public Airplane() {
        this.motor = new Motor();
        threadPoolExecutor.execute(mpu6050);
    }

    /**
     * 计算前后运动参数
     *
     * @param motorNums
     */
    public void calcForwardBackward(Double[] motorNums, double value) {
        if (value >= 0) {
            // 向前（正数）3/4 加油门,1/2 减油门
            motorNums[1] -= value;
            motorNums[2] -= value;
            motorNums[3] += value;
            motorNums[4] += value;
        } else {
            // 向后（负数）1/2 加油门，3/4 减油门
            motorNums[1] -= value;
            motorNums[2] -= value;
            motorNums[3] += value;
            motorNums[4] += value;
        }
    }

    /**
     * 计算左右运动
     *
     * @param motorNums
     */
    public void calcHorizontal(Double[] motorNums, double value) {
        if (value >= 0) {
            // 向右（正数）2/3 加油门,1/4 减油门
            motorNums[1] -= value;
            motorNums[4] -= value;
            motorNums[2] += value;
            motorNums[3] += value;
        } else {
            // 向左（负数）1/4 加油门，2/3 减油门
            motorNums[1] -= value;
            motorNums[4] -= value;
            motorNums[2] += value;
            motorNums[3] += value;
        }
    }

    /**
     * 计算旋转运动
     *
     * @param motorNums
     */
    public void calcRotate(Double[] motorNums) {
        double value = direction.getRotate();
        if (value >= 0) {
            // 右旋转（正数）2/4 加油门,1/3 减油门
            motorNums[1] -= value;
            motorNums[3] -= value;
            motorNums[2] += value;
            motorNums[4] += value;
        } else {
            // 左旋转（负数）1/2 加油门，3/4 减油门
            motorNums[1] -= value;
            motorNums[3] -= value;
            motorNums[2] += value;
            motorNums[4] += value;
        }
    }

    /**
     * 姿态微调
     *
     * @param motorNums
     */
    public void fineTuning(Double[] motorNums) {
        // 预设前进后退运行倾角参数
        double forwardBackward = direction.getForwardBackward();
        // 预设左右运行倾角参数
        double horizontal = direction.getHorizontal();

        Mpu6050.AngularResult angularResult = mpu6050.getAngularResult();
        // X轴的绝对倾斜角度
        double absoluteAngleX = angularResult.getAbsoluteAngleX();
        // Y轴的绝对倾斜角度
        double absoluteAngleY = angularResult.getAbsoluteAngleY();
        // X轴的相对倾斜角度
        double relativelyAngleX = angularResult.getRelativelyAngleX();
        // Y轴的相对倾斜角度
        double relativelyAngleY = angularResult.getRelativelyAngleY();

        calc(forwardBackward, relativelyAngleX, this::calcForwardBackward);
        calc(horizontal, relativelyAngleY, this::calcHorizontal);
    }

    public void calc(double control, double angle, BiConsumer<Double[], Double> fun) {
        int num = 5;
        double step = 0.005;
        if (Math.abs(control - angle) > num) {
            // 预计倾角与实际倾角产生一定的偏差，因此需要修正倾角偏差
            if (control > angle) {
                fun.accept(posture, step);
            } else {
                fun.accept(posture, -step);
            }
        }
    }

    /**
     * 电机引脚列表
     * .        前
     * .马达2        马达1
     * .马达3        马达4
     * .
     * .     Z轴   Y轴
     * .     |    /
     * .     |  /
     * .-----|/----------> X轴
     * .    /|
     * .  /  |
     * ./    |
     */
    @Override
    public void run() {
        run = true;
        while (run) {
            double vertical = direction.getVertical();
            if (vertical < 0.0 || vertical > 1.0) {
                logger.info("退出1，方向数据：{}", direction);
                try {
                    Thread.sleep(50);
                } catch (Exception ignore) {
                }
                continue;
            }
            if (!motorRun[0] || !motorRun[1]) {
                if (vertical >= 1.0) {
                    motorRun[0] = true;
                    vertical = 1.0;
                } else if (vertical <= 0.0) {
                    motorRun[1] = true;
                    vertical = 0.0;
                } else {
                    logger.info("退出2，方向数据：{}", direction);
                    try {
                        Thread.sleep(50);
                    } catch (Exception ignore) {
                    }
                    continue;
                }
                motor.setPwm(vertical, vertical, vertical, vertical);
                logger.info("电机校准，方向数据：{}", direction);
                try {
                    Thread.sleep(50);
                } catch (Exception ignore) {
                }
                continue;
            }
            /*
             * 电机位置
             * 2     1
             * 3     4
             */
            Double[] motorNums = new Double[]{0.0, vertical, vertical, vertical, vertical};
            boolean isRun = motor.dutyRatioCanRun(1, motorNums);
            if (isRun) {
                // 电机已经运行，可以进行姿态调整
                fineTuning(motorNums);
            }
            for (int i = 0; i < posture.length; i++) {
                if (posture[i] > 0.5) {
                    posture[i] = 0.5;
                }
                if (posture[i] < -0.5) {
                    posture[i] = -0.5;
                }
            }
            logger.debug("姿态调整状态：{} {} {}", isRun, Arrays.toString(motorNums), Arrays.toString(posture));
            for (int i = 0; i < motorNums.length; i++) {
                motorNums[i] += posture[i];
                if (motorNums[i] > 1.0) {
                    motorNums[i] = 1.0;
                }
                if (motorNums[i] < 0.0) {
                    motorNums[i] = 0.0;
                }
            }
            motor.setPwm(motorNums[1], motorNums[2], motorNums[3], motorNums[4]);
            try {
                Thread.sleep(30);
            } catch (Exception ignore) {
            }
        }
        Arrays.fill(posture, 0.0);
    }

    public void shutdown() {
        this.run = false;
        direction.reset();
        motor.shutdown();
        motorRun[0] = false;
        motorRun[1] = false;
    }

    /**
     * 提交飞机进入到运行状态
     */
    public void submitThreadRun() {
        direction.reset();
        motor.setPwm(-1d, -1d, -1d, -1d);
        threadPoolExecutor.execute(this);
        mpu6050.reset();
    }

    /**
     * 飞机运行状态
     *
     * @return
     */
    public Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        // GPS信息
        map.put("gps", gps);
        // 姿态信息
        map.put("mpu6050", mpu6050);
        // 方向信息
        map.put("direction", direction);

        map.put("motor", motor.status());

        return map;
    }
}
