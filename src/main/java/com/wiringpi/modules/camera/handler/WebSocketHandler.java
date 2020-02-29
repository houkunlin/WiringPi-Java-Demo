package com.wiringpi.modules.camera.handler;

import com.wiringpi.modules.camera.flv.FlvInfo;
import com.wiringpi.modules.camera.service.IFlvForwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * WebSocket处理器
 *
 * @author HouKunLin
 * @date 2020/2/18 0018 19:05
 */
public class WebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private final IFlvForwardService flvForwardService;

    public WebSocketHandler(IFlvForwardService flvForwardService) {
        this.flvForwardService = flvForwardService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        flvForwardService.onConnect(session);
    }

    public void readSend3(WebSocketSession session, InputStream inputStream) throws IOException {
        byte[] bytes = new byte[4096];
        int len;
        while ((len = inputStream.read(bytes)) != -1) {
            session.sendMessage(new BinaryMessage(bytes, 0, len, true));
        }
        logger.debug("发送数据完毕");
    }

    public void readSend4(WebSocketSession session, InputStream inputStream) throws Exception {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("test.flv");
            FlvInfo flvInfo = new FlvInfo(inputStream);
            flvInfo.writeFlvBytes(fileOutputStream::write);

            int len;
            byte[] bytes = new byte[4096];
            while ((len = inputStream.read(bytes)) != -1) {
                session.sendMessage(new BinaryMessage(bytes, 0, len, true));
            }
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
        logger.debug("发送数据完毕");
    }
}
