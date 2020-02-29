package com.wiringpi.modules.led2;

import com.wiringpi.gpio.Gpio;
import com.wiringpi.pin.IPin;
import lombok.ToString;

/**
 * 双色LED灯实验
 *
 * @author HouKunLin
 * @date 2020/2/13 0013 14:18
 */
@ToString
public class Led2 {
    /**
     * 红色管脚
     */
    private Gpio red;
    /**
     * 绿色管脚
     */
    private Gpio green;

    public Led2(Gpio red, Gpio green) {
        this.red = red;
        this.green = green;
        red.softPwmCreate(0, 100);
        green.softPwmCreate(0, 100);
    }

    public Led2(IPin red, IPin green) {
        this(red.gpio(), green.gpio());
    }

    public void red() {
        red.softPwmWrite(100);
        green.softPwmWrite(0);
    }

    public void green() {
        red.softPwmWrite(0);
        green.softPwmWrite(100);
    }

    public void red(int value) {
        red.softPwmWrite(value);
    }

    public void green(int value) {
        green.softPwmWrite(value);
    }

    public void set(int redValue, int greenValue) {
        red(redValue);
        green(greenValue);
    }
}
