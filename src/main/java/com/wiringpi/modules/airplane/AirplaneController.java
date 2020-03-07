package com.wiringpi.modules.airplane;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiringpi.modules.airplane.dto.DirectionDTO;
import com.wiringpi.modules.airplane.dto.Motor;
import com.wiringpi.modules.airplane.dto.MotorDebugDTO;
import com.wiringpi.modules.airplane.dto.Power;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 超声波测距
 *
 * @author HouKunLin
 * @date 2020/2/22 0022 23:51
 */
@RestController
@RequestMapping
public class AirplaneController {
    private static final Logger logger = LoggerFactory.getLogger(AirplaneController.class);

    private final Airplane airplane;
    private final Power power;
    private final ObjectMapper objectMapper;

    private final SimpMessagingTemplate messagingTemplate;

    public AirplaneController(SimpMessagingTemplate messagingTemplate, Airplane airplane, Power power, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.airplane = airplane;
        this.power = power;
        this.objectMapper = objectMapper;
    }

    /**
     * 设置电机电源
     *
     * @param map
     * @return
     */
    @MessageMapping("airplane/power")
    public void power(Map<String, Object> map) {
        logger.info("设置电机电源参数：{}", map);
        Boolean value = (Boolean) map.get("value");
        if (value) {
            power.startup();
            airplane.submitThreadRun();
        } else {
            power.shutdown();
            airplane.shutdown();
        }
    }

    /**
     * 操作飞机飞行的数据参数
     *
     * @param dto
     */
    @MessageMapping("airplane/direction")
    public void direction(DirectionDTO dto) {
        airplane.getDirection().refresh(dto);
    }

    /**
     * 设置电机的占空比
     *
     * @param dto
     * @return
     * @throws InterruptedException
     */
    @MessageMapping("airplane/motor/dutyCycle")
    @GetMapping("airplane/motor/dutyCycle")
    public void motor(MotorDebugDTO dto) throws InterruptedException {
        logger.info("设置电机占空比：{}", dto);
        int index = dto.getIndex();
        double value = dto.getValue();
        Double[] motorNums = new Double[]{null, null, null, null, null};
        if (index > motorNums.length || index < 1) {
            for (int i = 1; i <= 4; i++) {
                motorNums[i] = value;
            }
        } else {
            motorNums[index] = value;
        }
        airplane.getMotor().setPwm(motorNums[1], motorNums[2], motorNums[3], motorNums[4]);
    }

    /**
     * 紧急制动。在紧急情况下一键关闭飞机所有电机，使电机不再运行，但是不会关闭电源，为后续启动做准备。
     */
    @MessageMapping("airplane/emergencyBraking")
    @GetMapping("airplane/emergencyBraking")
    public void emergencyBraking() {
        DirectionDTO direction = airplane.getDirection();
        direction.setVertical(0.0);
        direction.setHorizontal(0.0);
        direction.setForwardBackward(0.0);
        direction.setRotate(0.0);
        airplane.getMotor().shutdown();
    }

    /**
     * 定时向展示端报告飞机的飞行数据
     */
    @Scheduled(fixedRate = 500)
    public void callback() throws JsonProcessingException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("airplane", airplane.status());
        map.put("power", power);

        messagingTemplate.convertAndSend("/topic/airplane/status", objectMapper.writeValueAsString(map));
    }
}
