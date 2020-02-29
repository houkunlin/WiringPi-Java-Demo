package com.wiringpi.modules.camera;

import com.wiringpi.modules.camera.service.IFlvForwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 视频处理
 *
 * @author HouKunLin
 * @date 2020/2/15 0015 23:54
 */
@RestController
@RequestMapping("/raspberry-pi/camera")
public class CameraController {
    private static final Logger logger = LoggerFactory.getLogger(CameraController.class);
    private final HttpServletRequest request;
    private final IFlvForwardService flvForwardService;

    public CameraController(HttpServletRequest request, IFlvForwardService flvForwardService) {
        this.request = request;
        this.flvForwardService = flvForwardService;
    }

    @RequestMapping("push")
    public ResponseEntity<Object> push() throws Exception {
        logger.debug("收到视频流推送");
        flvForwardService.forwardFlv(request.getInputStream());
        return new ResponseEntity<>(HttpStatus.OK);
    }
/*
    @GetMapping("start")
    public ResponseEntity<Object> start(FfmpegDTO ffmpegDTO) throws IOException {
        logger.debug("开启视频流推送：{} ", ffmpegDTO);
        flvForwardService.startFFMpeg(ffmpegDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("stop")
    public ResponseEntity<Object> stop() {
        logger.debug("主动结束视频推流");
        flvForwardService.stopFFMpeg();
        return new ResponseEntity<>(HttpStatus.OK);
    }*/
}
