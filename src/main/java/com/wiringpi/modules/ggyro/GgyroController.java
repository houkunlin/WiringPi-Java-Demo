package com.wiringpi.modules.ggyro;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("ggyro")
public class GgyroController {
    private final SimpMessagingTemplate messagingTemplate;
    private Ggyro ggyro;

    public GgyroController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void post() {
        ggyro = new Ggyro();
    }

    @GetMapping("run")
    public ResponseEntity<Object> run() {
        return new ResponseEntity<>(getInfo(), HttpStatus.OK);
    }

    private Map<String, Object> getInfo() {
        Map<String, Object> map = new LinkedHashMap<>();
        ggyro.refresh1();
        map.put("map1", ggyro.toMap());
        ggyro.refresh2();
        map.put("map2", ggyro.toMap());
        return map;
    }

    /**
     * 定时推送消息
     */
    @Scheduled(fixedRate = 100)
    public void callback() {
        // 发现消息
        messagingTemplate.convertAndSend("/topic/ggyro", getInfo());
    }
}
