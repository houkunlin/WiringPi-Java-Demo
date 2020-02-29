package com.wiringpi.modules.camera.service;

import com.wiringpi.modules.camera.dto.FfmpegDTO;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;

/**
 * FLV 视频转发处理
 *
 * @author HouKunLin
 * @date 2020/2/18 0018 17:26
 */
public interface IFlvForwardService {

    void startFFMpeg(FfmpegDTO ffmpegDTO) throws IOException;

    void stopFFMpeg();

    /**
     * 转发FLV数据
     *
     * @param inputStream
     */
    void forwardFlv(InputStream inputStream) throws Exception;

    /**
     * 一个新的连接
     *
     * @param session
     */
    void onConnect(WebSocketSession session) throws Exception;

    /**
     * 把数据转发给 WebSocket
     *
     * @param message
     */
    void forwardToWebSocket(WebSocketMessage<?> message) throws IOException;
}
