package com.wiringpi.modules.music;

import com.wiringpi.gpio.Gpio;
import com.wiringpi.jni.WiringPi;
import com.wiringpi.pin.BcmPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * 无源蜂鸣器实验。
 *
 * @author HouKunLin
 * @date 2020/2/22 0022 14:29
 */
@RestController
@RequestMapping("music")
public class MusicController {
    private static final Logger logger = LoggerFactory.getLogger(MusicController.class);
    private Gpio buzPin;

    private int CL1 = 131;
    private int CL2 = 147;
    private int CL3 = 165;
    private int CL4 = 175;
    private int CL5 = 196;
    private int CL6 = 221;
    private int CL7 = 248;

    private int CM1 = 262;
    private int CM2 = 294;
    private int CM3 = 330;
    private int CM4 = 350;
    private int CM5 = 393;
    private int CM6 = 441;
    private int CM7 = 495;

    private int CH1 = 525;
    private int CH2 = 589;
    private int CH3 = 661;
    private int CH4 = 700;
    private int CH5 = 786;
    private int CH6 = 882;
    private int CH7 = 990;
    private int song_1[] = {
            CM3, CM5, CM6, CM3, CM2, CM3,
            CM5, CM6, CH1, CM6, CM5, CM1,
            CM3, CM2, CM2, CM3, CM5, CM2,
            CM3, CM3, CL6, CL6, CL6, CM1,
            CM2, CM3, CM2, CL7, CL6, CM1, CL5};

    private int beat_1[] = {1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1,
            1, 1, 1, 1, 1, 1, 3};


    private int song_2[] = {
            CM1, CM1, CM1, CL5, CM3, CM3,
            CM3, CM1, CM1, CM3, CM5, CM5,
            CM4, CM3, CM2, CM2, CM3, CM4,
            CM4, CM3, CM2, CM3, CM1, CM1,
            CM3, CM2, CL5, CL7, CM2, CM1};

    private int beat_2[] = {1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 2, 1, 1, 1, 3, 1, 1, 1, 3, 3, 2, 3};

    @PostConstruct
    public void post() {
        buzPin = BcmPin.p17.gpio();
        buzPin.softToneCreate();
    }

    @GetMapping("run")
    public ResponseEntity<Object> run() throws InterruptedException {

        while (true) {
            logger.debug("music is being played...");

            for (int i = 0; i < song_1.length; i++) {
                buzPin.softToneWrite(song_1[i]);
                WiringPi.delay(beat_1[i] * 500);
                // sleep(beat_1[i] * 500);
            }

            for (int i = 0; i < song_2.length; i++) {
                buzPin.softToneWrite(song_2[i]);
                WiringPi.delay(beat_2[i] * 500);
                // sleep(beat_2[i] * 500);
            }
        }

        // return new ResponseEntity<>(HttpStatus.OK);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
            logger.debug("睡眠{}完成", ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
