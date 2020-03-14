package com.wiringpi.modules.airplane.dto;

import com.wiringpi.demo.hardware.Pca9685;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 电机
 * <p>
 * 电机调整公式：占空比百分比 * ( 2.1 - 0.7 ) + 0.7 = 占空比百分比 * 1.4 + 0.7
 * 占空比为 0% - 100% 取小数值，0%为关闭电机，100%电机最大值
 * 电机油门，油门取值 0% - 100%，油门百分比 * 1.4 + 0.7
 * </p>
 *
 * @author HouKunLin
 * @date 2020/2/24 0024 10:56
 */
@Data
public class Motor {
    /**
     * 电机最大转速的高电平时间（毫秒）
     */
    private static final int MOTOR_MAX_HIGH_LEVEL_TIME_MS = 2200;
    /**
     * 电机停止运行的高电平时间（毫秒）
     */
    private static final int MOTOR_MIN_HIGH_LEVEL_TIME_MS = 840;
    /**
     * 调试高电平时间
     */
    private double debugHighLevelTime = -1;
    /**
     * 可允许进行计算的高电平时长：最大油门 - 最小油门
     */
    private int motorMidHighLevelTimeMs;
    /**
     * 电机数
     */
    private int motorNum = 4;

    private Pca9685 pca9685;

    public Motor() {
        pca9685 = new Pca9685(0x40);
        pca9685.setPWMFreq(50);
        motorMidHighLevelTimeMs = MOTOR_MAX_HIGH_LEVEL_TIME_MS - MOTOR_MIN_HIGH_LEVEL_TIME_MS;
    }

    /**
     * 设置电机占空比
     *
     * @param dutyRatios 占空比列表
     */
    public void setPwm(Double... dutyRatios) {
        if (dutyRatios.length != motorNum) {
            return;
        }
        for (int i = 0; i < dutyRatios.length; i++) {
            Double dutyRatio = dutyRatios[i];
            if (dutyRatio == null) {
                continue;
            }
            pca9685.setServoPulse(i, calcHighLevelTimeUs(dutyRatio));
        }
    }

    /**
     * 设置电机PWM
     *
     * @param channel 电机索引
     * @param pulse   高电平时间（微秒）
     */
    public void setServoPulse(int channel, int pulse) {
        pca9685.setServoPulse(channel, pulse);
    }

    /**
     * 计算占空比对应的高电平时间
     *
     * @return
     */
    public int calcHighLevelTimeUs(Double dutyRatio) {
        // 0.7 为电机不报警的最低高电平时间，只要小于0.7的高电平电机就报警
        double highLevelTime = dutyRatio * motorMidHighLevelTimeMs + MOTOR_MIN_HIGH_LEVEL_TIME_MS;
        if (highLevelTime >= MOTOR_MAX_HIGH_LEVEL_TIME_MS) {
            // 超过最大高电平可能会引起电机转速下降的问题
            highLevelTime = MOTOR_MAX_HIGH_LEVEL_TIME_MS;
        } else if (highLevelTime <= 0) {
            // 高电平时间不能为负数。这里不使用 MOTOR_MIN_HIGH_LEVEL_TIME_MS 的原因
            // 是因为刚开始运行的时候需要对电调进行油门校正，油门校正之前的值会为 0
            highLevelTime = 0;
        }
        return (int) highLevelTime;
    }

    /**
     * 关闭电机
     */
    public void shutdown() {
        for (int i = 0; i < motorNum; i++) {
            pca9685.setServoPulse(i, MOTOR_MIN_HIGH_LEVEL_TIME_MS);
        }
    }

    public void shutdown(int i) {
        pca9685.setServoPulse(i, MOTOR_MIN_HIGH_LEVEL_TIME_MS);
    }

    /**
     * 判断一个PWM是否能够使电机运行起来
     *
     * @param pwm PWM值
     * @return 结果
     */
    public boolean isRun(int pwm) {
        return pwm <= MOTOR_MAX_HIGH_LEVEL_TIME_MS && pwm > MOTOR_MIN_HIGH_LEVEL_TIME_MS + 80;
    }

    /**
     * 判断某个占空比是否能够使电机运行起来
     *
     * @param dutyRatio 暂空比
     * @return 结果
     */
    public boolean dutyRatioCanRun(double dutyRatio) {
        return isRun(calcHighLevelTimeUs(dutyRatio));
    }

    /**
     * 判断占空比是否能够使所有电机运行起来
     *
     * @param offset     暂空比列表的起始位置
     * @param dutyRatios 暂空比列表
     * @return 结果
     */
    public boolean dutyRatioCanRun(int offset, Double... dutyRatios) {
        for (int i = offset; i < dutyRatios.length; i++) {
            if (!dutyRatioCanRun(dutyRatios[i])) {
                return false;
            }
        }
        return true;
    }

    public Object status() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < motorNum; i++) {
            Map<String, Object> map = new LinkedHashMap<>();
            int pwm = pca9685.readPwm(i);
            map.put("index", i);
            map.put("value", pwm);
            // map.put("run", isRun(pwm));
            list.add(map);
        }
        result.put("motors", list);
        result.put("maxValue", MOTOR_MAX_HIGH_LEVEL_TIME_MS);
        result.put("midValue", motorMidHighLevelTimeMs);
        result.put("minValue", MOTOR_MIN_HIGH_LEVEL_TIME_MS);
        return result;
    }
}
