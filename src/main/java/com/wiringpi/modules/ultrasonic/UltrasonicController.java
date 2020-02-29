package com.wiringpi.modules.ultrasonic;

import com.wiringpi.pin.BcmPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

/**
 * 超声波测距
 *
 * @author HouKunLin
 * @date 2020/2/22 0022 23:51
 */
@RestController
@RequestMapping("ultrasonic")
public class UltrasonicController {
    private static final Logger logger = LoggerFactory.getLogger(UltrasonicController.class);

    private Ultrasonic ultrasonic;

    private final SimpMessagingTemplate messagingTemplate;

    public UltrasonicController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void post() {
        ultrasonic = new Ultrasonic(BcmPin.p17, BcmPin.p18);
    }

    @GetMapping("run")
    public ResponseEntity<Object> run() throws InterruptedException {
        while (true) {
            // 发现消息
            BigDecimal distance = ultrasonic.getDistance();
            logger.debug("测距：{}cm", distance);
            Thread.sleep(500);
            // messagingTemplate.convertAndSend("/topic/ultrasonic", distance);
        }

        // return new ResponseEntity<>(ultrasonic.getDistance(), HttpStatus.OK);
    }

    /**
     * 定时推送消息
     */
    // @Scheduled(fixedRate = 500)
    // public void callback() {
    // }
}
