package com.wiringpi.modules.airplane.dto;

import com.wiringpi.gpio.Gpio;
import com.wiringpi.pin.modes.PinMode;
import lombok.Data;

import java.util.Date;

/**
 * 电机电源
 *
 * @author HouKunLin
 * @date 2020/2/25 0025 22:58
 */
@Data
public class Power {
    private Gpio gpio;
    private boolean open;
    private Date startTime;
    private Date endTime;

    public Power(Gpio gpio) {
        this.gpio = gpio;
        this.gpio.pinMode(PinMode.OUTPUT);
    }

    public void startup() {
        this.open = true;
        this.startTime = new Date();
        this.endTime = null;
        this.gpio.high();
    }

    public void shutdown() {
        this.open = false;
        this.endTime = new Date();
        this.gpio.low();
    }
}
