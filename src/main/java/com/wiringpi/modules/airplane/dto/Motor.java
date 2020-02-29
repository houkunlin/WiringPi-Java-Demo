package com.wiringpi.modules.airplane.dto;

import com.wiringpi.gpio.Gpio;
import com.wiringpi.jni.WiringPi;
import com.wiringpi.pin.modes.PinMode;
import lombok.Data;

import java.util.LinkedHashMap;
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
public class Motor implements Runnable {
    /**
     * 电机最大转速的高电平时间（毫秒）
     */
    private static final double MOTOR_MAX_HIGH_LEVEL_TIME_MS = 2.1;
    /**
     * 电机停止运行的高电平时间（毫秒）
     */
    private static final double MOTOR_MIN_HIGH_LEVEL_TIME_MS = 0.7;
    /**
     * 周期（微秒）：20*1000微秒 = 20毫秒。
     * 减了150是因为调试的时候发现运行完一个周期需要20150000到20180000纳秒，平均维持在20150800纳秒左右，如果减了150之后平均一个周期有20006500
     */
    private static final int CYCLE = 20 * 1000 - 151;
    /**
     * 电机的GPIO脚
     */
    private Gpio gpio;
    /**
     * 占空比 0.0 - 1.0
     * -0.5表示默认没有高电平，使公式值等于0，即高电平时间为0毫秒
     */
    private double dutyRatio = -0.5;
    /**
     * 姿态微调，与油门结合在一起
     */
    private double posture = 0.0;
    /// private AtomicInteger test;
    /**
     * 标记是否运行
     */
    private boolean run = false;
    /**
     * 调试高电平时间
     */
    private double debugHighLevelTime = -1;
    /**
     * 运行时的动态周期计算信息（纳秒）
     */
    private long runtimeCycle = 0;
    /**
     * 电机名称
     */
    private String title;

    public Motor(Gpio gpio, String title) {
        this.gpio = gpio;
        this.gpio.pinMode(PinMode.OUTPUT);
        this.title = title;
    }

    @Override
    public void run() {
        run = true;
        while (run) {
            double highLevelTime;
            if (debugHighLevelTime < 0) {
                // 非调试模式
                highLevelTime = calcHighLevelTimeUs();
                if (highLevelTime >= MOTOR_MAX_HIGH_LEVEL_TIME_MS) {
                    // 超过最大高电平可能会引起电机转速下降的问题
                    highLevelTime = MOTOR_MAX_HIGH_LEVEL_TIME_MS;
                } else if (highLevelTime <= 0) {
                    // 高电平时间不能为负数。这里不使用 MOTOR_MIN_HIGH_LEVEL_TIME_MS 的原因
                    // 是因为刚开始运行的时候需要对电调进行油门校正，油门校正之前的值会为 0
                    highLevelTime = 0;
                }
            } else {
                // 占空比调试模式
                highLevelTime = debugHighLevelTime;
            }
            long nanoTime = System.nanoTime();
            runOneCycle((int) (highLevelTime * 1000));
            runtimeCycle = System.nanoTime() - nanoTime;
        }
        // 设置引脚为低电平
        this.gpio.low();
        // 重置占空比为-0.5，只为 calcHighLevelTimeUs() 方法计算结果取值为0
        this.dutyRatio = -0.5;
    }

    private void runOneCycle(int highLevelTimeUs) {
        if (highLevelTimeUs <= 0) {
            gpio.low();
            WiringPi.delayMicroseconds(CYCLE);
            return;
        }
        gpio.high();
        WiringPi.delayMicroseconds(highLevelTimeUs);
        gpio.low();
        WiringPi.delayMicroseconds(CYCLE - highLevelTimeUs);
    }

    /**
     * 获取当前占空比
     *
     * @return
     */
    private double calcDutyRatio() {
        return dutyRatio + posture;
    }

    /**
     * 计算占空比对应的高电平时间
     *
     * @return
     */
    private double calcHighLevelTimeUs() {
        // 0.7 为电机不报警的最低高电平时间，只要小于0.7的高电平电机就报警
        return calcDutyRatio() * 1.4 + 0.7;
    }

    /**
     * 设置运行标记未false
     */
    public void shutdown() {
        this.run = false;
    }

    /**
     * 微调油门大小，在原有数值上进行调整
     *
     * @param gasPedal 大于 0 向上调整，小于 0 向下调整。例如 0.05 向上调整 5%
     * @return
     */
    @Deprecated
    public boolean adjustGasPedal(double gasPedal) {
        // 计算微调后的占空比
        double temp = calcDutyRatio() + gasPedal;
        if (temp > 1.0 || temp < 0.0) {
            // 如果微调后的占空比不在0.0-1.0之间则调整失败
            return false;
        }
        this.dutyRatio += gasPedal;
        return true;
    }

    /**
     * 微调电机占空比，在原有的占空比上进行微调。
     *
     * @param posture 大于 0 向上调整，小于 0 向下调整。例如 0.05 向上调整 5%
     * @return
     */
    @Deprecated
    public boolean adjustPosture(double posture) {
        // 计算微调后的占空比
        double temp = calcDutyRatio() + posture;
        if (temp > 1.0 || temp < 0.0) {
            // 如果微调后的占空比不在0.0-1.0之间则调整失败
            return false;
        }
        this.posture += posture;
        return true;
    }

    @Deprecated
    private Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", title);
        map.put("run", run);
        map.put("gpio", gpio);
        map.put("cycle", CYCLE);
        map.put("runtimeCycle", runtimeCycle / 1000);
        map.put("posture", posture);
        map.put("dutyRatio", dutyRatio);

        return map;
    }
}
