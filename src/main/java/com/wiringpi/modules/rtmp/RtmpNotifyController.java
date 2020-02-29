package com.wiringpi.modules.rtmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * RTMP 推流事件触发
 *
 * @author HouKunLin
 * @date //  :
 */
@RestController
@RequestMapping("/rtmp")
public class RtmpNotifyController {
    private static final Logger logger = LoggerFactory.getLogger(RtmpNotifyController.class);
    private final HttpServletRequest request;

    public RtmpNotifyController(HttpServletRequest request) {
        this.request = request;
    }

    @GetMapping
    public ResponseEntity<Object> defaultEvent(RtmpDTO rtmpDTO, Map<String, Object> map) {
        logger.debug("默认的未指定事件：{} , {}", rtmpDTO, map);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "call=connect")
    public ResponseEntity<Object> onConnect(RtmpDTO rtmpDTO, Map<String, Object> map) {
        logger.debug("客户端连接事件: {} , {}", rtmpDTO, map);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "call=play")
    public ResponseEntity<Object> onPlay(RtmpDTO rtmpDTO, Map<String, Object> map) {
        logger.debug("播放事件: {} , {}", rtmpDTO, map);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "call=publish")
    public ResponseEntity<Object> onPublish(RtmpDTO rtmpDTO, String password) {
        logger.debug("推送事件: {}", rtmpDTO);
        if (!"HouKunLin".equals(password)) {
            // 密码不正确，禁止进行推流
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "call=done")
    public ResponseEntity<Object> onDone(RtmpDTO rtmpDTO, Map<String, Object> map) {
        logger.debug("完成事件: {} , {}", rtmpDTO, map);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "call=play_done")
    public ResponseEntity<Object> onPlayDone(RtmpDTO rtmpDTO, Map<String, Object> map) {
        logger.debug("播放完成事件: {} , {}", rtmpDTO, map);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "call=publish_done")
    public ResponseEntity<Object> onPublishDone(RtmpDTO rtmpDTO, Map<String, Object> map) {
        logger.debug("推送完成事件: {} , {}", rtmpDTO, map);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "call=record_done")
    public ResponseEntity<Object> onRecordDone(RtmpDTO rtmpDTO, Map<String, Object> map) {
        logger.debug("录制完成事件: {} , {}", rtmpDTO, map);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(params = "call=update")
    public ResponseEntity<Object> onUpdate(RtmpDTO rtmpDTO, Map<String, Object> map) {
        logger.debug("更新事件: {} , {}", rtmpDTO, map);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
