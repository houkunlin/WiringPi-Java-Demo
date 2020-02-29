package com.wiringpi.modules.camera.service.impl;

import com.wiringpi.modules.camera.dto.FfmpegDTO;
import com.wiringpi.modules.camera.flv.FlvInfo;
import com.wiringpi.modules.camera.service.IFlvForwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * FLV 数据转发 Service
 *
 * @author HouKunLin
 * @date 2020/2/18 0018 17:31
 */
@Service
public class FlvForwardServiceImpl implements IFlvForwardService {
    private static final Logger logger = LoggerFactory.getLogger(FlvForwardServiceImpl.class);
    private FlvInfo flvInfo;
    private WebSocketSession session;
    private Process process;

    @Async
    @Override
    public void startFFMpeg(FfmpegDTO ffmpegDTO) throws IOException {
        if (process != null) {
            return;
        }
        String[] commands = ffmpegDTO.toCommand();
        String command = String.join(" ", commands);
        logger.info("推流命令：{}", command);
        process = Runtime.getRuntime().exec(command);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InputStream inputStream;
        if (process.isAlive()) {
            // inputStream = process.getInputStream();
            logger.debug("经检测推流正在继续");
        } else {
            inputStream = process.getErrorStream();
            logger.debug("推流命令执行出错输出信息：{}", inputStream);
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }
    }

    @Override
    public void stopFFMpeg() {
        if (process != null) {
            process.destroy();
            try {
                process.waitFor(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("停止推流，当前状态：{}", process.isAlive());
            process = null;
        }
    }

    @Override
    public void forwardFlv(InputStream inputStream) throws Exception {
        logger.debug("开始处理推流数据");
        flvInfo = new FlvInfo(inputStream);
        logger.debug("获取基本的视频数据信息完毕，正在读取数据和进行数据转发");
        flvInfo.writeFlvBytes((bytes) -> forwardToWebSocket(new BinaryMessage(bytes)));

        int len;
        byte[] bytes = new byte[4096];
        while (sessionIsOk() && (len = inputStream.read(bytes)) != -1) {
            forwardToWebSocket(new BinaryMessage(bytes, 0, len, true));
        }
        inputStream.close();
        stopFFMpeg();
        flvInfo = null;
    }

    @Async
    @Override
    public void onConnect(WebSocketSession session) throws Exception {
        logger.debug("新的 WebSocket 连接进入，正在发送补充信息");
        if (flvInfo != null) {
            flvInfo.writeFlvStartInfoBytes(bytes -> session.sendMessage(new BinaryMessage(bytes)));
        }
        logger.debug("新的 WebSocket 连接进入，补充信息发送完毕");
        this.session = session;
        this.startFFMpeg(new FfmpegDTO());
    }

    @Override
    public void forwardToWebSocket(WebSocketMessage<?> message) throws IOException {
        try {
            if (session.isOpen()) {
                session.sendMessage(message);
            } else {
                logger.debug("会话 {} 被关闭", session.getId());
                session = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("发送数据失败", e);
            try {
                session.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                logger.error("关闭连接失败", ex);
            }
        }
    }

    private boolean sessionIsOk() {
        return session != null && session.isOpen();
    }
}
