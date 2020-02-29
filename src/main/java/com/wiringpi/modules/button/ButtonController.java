package com.wiringpi.modules.button;

import com.wiringpi.gpio.Gpio;
import com.wiringpi.pin.BcmPin;
import com.wiringpi.pin.modes.PinMode;
import com.wiringpi.pin.modes.PinValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * 按钮实验
 *
 * @author HouKunLin
 * @date 2020/2/21 0021 23:02
 */
@RestController
@RequestMapping("button")
public class ButtonController {
    private static final Logger logger = LoggerFactory.getLogger(ButtonController.class);
    private Gpio btnPin;
    private Gpio gPin;
    private Gpio rPin;
    private PinValue lastStatus;

    @PostConstruct
    public void post() {
        btnPin = BcmPin.p17.gpio(PinMode.INPUT, "按钮");
        gPin = BcmPin.p18.gpio(PinMode.OUTPUT, "绿色");
        rPin = BcmPin.p27.gpio(PinMode.OUTPUT, "红色");
    }

    @GetMapping("run")
    public ResponseEntity<Object> run() {
        logger.debug("按钮：{}", btnPin);
        logger.debug("绿色：{}", gPin);
        logger.debug("红色：{}", rPin);
        lastStatus = PinValue.LOW;

        while (true) {
            PinValue pinValue = btnPin.digitalRead();
            if (pinValue == PinValue.LOW && lastStatus == PinValue.HIGH) {
                toggle(pinValue);
                logger.debug("已按下按钮！");
            } else if (pinValue == PinValue.HIGH && lastStatus == PinValue.LOW) {
                toggle(pinValue);
                logger.debug("请按下按钮");
            }
        }

    }

    private void toggle(PinValue pinValue) {
        lastStatus = pinValue;
        if (pinValue == PinValue.LOW) {
            // 按下按钮
            rPin.high();
            gPin.low();
        } else {
            // 松开按钮
            rPin.low();
            gPin.high();
        }
    }
}
