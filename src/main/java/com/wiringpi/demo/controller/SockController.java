package com.wiringpi.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class SockController {
    private static final Logger logger = LoggerFactory.getLogger(SockController.class);
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 收到消息记数
     */
    private AtomicInteger count = new AtomicInteger(0);

    /**
     * MessageMapping 指定要接收消息的地址，类似@RequestMapping。除了注解到方法上，也可以注解到类上
     * SendTo 默认 消息将被发送到与传入消息相同的目的地
     * 消息的返回值是通过{@link org.springframework.messaging.converter.MessageConverter}进行转换
     */
    @MessageMapping("/send")
    @SendTo("/topic/recv")
    public Object broadcast(Map<String, Object> map) {
        logger.info("receive message = {}", map);
        return map;
    }

    /**
     * MessageMapping 指定要接收消息的地址，类似@RequestMapping。除了注解到方法上，也可以注解到类上
     * SendTo 默认 消息将被发送到与传入消息相同的目的地
     * 消息的返回值是通过{@link org.springframework.messaging.converter.MessageConverter}进行转换
     */
    @MessageMapping("/test1")
    public Object test1(Map<String, Object> map) {
        logger.info("receive message = {}", map);
        logger.info("receive message = {}", map.get("value"));
        logger.info("receive message = {}", map.get("value").getClass());
        return map;
    }

    /**
     * MessageMapping 指定要接收消息的地址，类似@RequestMapping。除了注解到方法上，也可以注解到类上
     * SendTo 默认 消息将被发送到与传入消息相同的目的地
     * 消息的返回值是通过{@link org.springframework.messaging.converter.MessageConverter}进行转换
     */
    @MessageMapping("/test2")
    public Object test2(@RequestParam(defaultValue = "false") boolean value) {
        logger.info("receive message = {}", value);
        return null;
    }

    /**
     * 定时推送消息
     */
    @Scheduled(fixedRate = 1000)
    public void callback() {
        // 发现消息
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = df.format(new Date());
        messagingTemplate.convertAndSend("/topic/callback", "定时推送消息时间: " + format);
    }
}
