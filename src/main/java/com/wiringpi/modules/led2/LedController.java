package com.wiringpi.modules.led2;

import com.wiringpi.pin.BcmPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LED 灯控制
 *
 * @author HouKunLin
 * @date 2020/2/13 0013 1:31
 */
@RestController
@RequestMapping("/led")
public class LedController {
    private static final Logger logger = LoggerFactory.getLogger(LedController.class);
    private Led2 led = new Led2(BcmPin.p17, BcmPin.p18);

    @GetMapping("status")
    public String status() {
        return "ok:" + led;
    }

    @GetMapping("red")
    public String red() {
        led.red();
        return "ok";
    }

    @GetMapping("green")
    public String green() {
        led.green();
        return "ok";
    }

    @GetMapping("set")
    public String other(int red, int green) {
        led.set(red, green);
        return "OK";
    }

    @GetMapping("cut")
    public String cut() {
        for (int i = 0; i <= 10; i++) {
            led.set(i * 10, 100 - i * 10);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "ok";
    }
}
