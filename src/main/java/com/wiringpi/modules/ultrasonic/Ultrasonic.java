package com.wiringpi.modules.ultrasonic;

import com.wiringpi.gpio.Gpio;
import com.wiringpi.pin.IPin;
import com.wiringpi.pin.modes.PinMode;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.wiringpi.jni.WiringPi.delayMicroseconds;

/**
 * 超声波测距
 *
 * @author HouKunLin
 * @date 2020/2/23 0023 0:15
 */
@Data
public class Ultrasonic {
    private static final Logger logger = LoggerFactory.getLogger(Ultrasonic.class);
    private Gpio trig;
    private Gpio echo;

    public Ultrasonic(Gpio trig, Gpio echo) {
        this.trig = trig;
        this.echo = echo;
        trig.pinMode(PinMode.OUTPUT);
        echo.pinMode(PinMode.INPUT);
    }

    public Ultrasonic(IPin trig, IPin echo) {
        this.trig = trig.gpio();
        this.echo = echo.gpio();
    }

    /**
     * 获取距离
     *
     * @return 距离
     */
    public BigDecimal getDistance() {
        long time1, time2;
        trig.low();
        delayMicroseconds(2);
        trig.high();
        //发出超声波脉冲
        delayMicroseconds(10);
        trig.low();

        while (echo.digitalRead().isLow()) {
            // 低电平，有回声，可能遇到上一个信息未处理完成，此时等待上一次的回声过后再处理
        }
        time1 = System.nanoTime();

        while (echo.digitalRead().isHigh()) {
            // 高电平，没有回声，持续监听，直到变为低电平（有回声）
        }
        time2 = System.nanoTime();

        // 求距离：S=VT
        // 纳秒 / 1000_000_000d = 秒
        // 秒 * 3400_00 = 秒 * 3400m * 100 = 距离（厘米）
        double distance = (time2 - time1) * 1.0 / 1000_000_000d * 340_00 / 2;
        BigDecimal bigDecimal = new BigDecimal(distance);
        return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    }
}
