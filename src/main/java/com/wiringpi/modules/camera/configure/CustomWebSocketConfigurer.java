package com.wiringpi.modules.camera.configure;

import com.wiringpi.modules.camera.handler.WebSocketHandler;
import com.wiringpi.modules.camera.service.IFlvForwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 自定义 WebSocket 配置
 *
 * @author HouKunLin
 * @date 2020/2/18 0018 19:07
 */
@Configuration
@EnableWebSocket
public class CustomWebSocketConfigurer implements WebSocketConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketConfigurer.class);
    private final IFlvForwardService flvForwardService;

    public CustomWebSocketConfigurer(@Autowired(required = false) IFlvForwardService flvForwardService) {
        this.flvForwardService = flvForwardService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        if (flvForwardService != null) {
            registry.addHandler(new WebSocketHandler(flvForwardService), "/raspberry-pi/camera")
                    .setAllowedOrigins("*");
        }
    }

}