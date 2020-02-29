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
     * @throws InterruptedException
     */
    @MessageMapping("airplane/power")
    public void power(Map<String, Object> map) throws InterruptedException {
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
        Motor[] motors = airplane.getMotors();
        if (index > motors.length || index < 1) {
            for (Motor motor : motors) {
                motor.setDutyRatio(value);
            }
        } else {
            motors[index - 1].setDutyRatio(value);
        }
    }

    /**
     * 设置电机的高电平时间值
     *
     * @param dto
     * @return
     * @throws InterruptedException
     */
    @MessageMapping("airplane/motor/debug")
    @GetMapping("airplane/motor/debug")
    public void debug(MotorDebugDTO dto) throws InterruptedException {
        logger.info("调试电机高电平时间参数：{}", dto);
        int index = dto.getIndex();
        double value = dto.getValue();
        Motor[] motors = airplane.getMotors();
        if (index > motors.length || index < 1) {
            for (Motor motor : motors) {
                motor.setDebugHighLevelTime(value);
            }
        } else {
            motors[index - 1].setDebugHighLevelTime(value);
        }
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
        for (Motor motor : airplane.getMotors()) {
            motor.setDebugHighLevelTime(-1);
            motor.setDutyRatio(0);
            motor.setPosture(0);
        }
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
