package com.wiringpi.demo.endpoint;

import com.wiringpi.jni.WiringPi;
import com.wiringpi.pin.BcmPin;
import com.wiringpi.pin.IPin;
import com.wiringpi.pin.PhysPin;
import com.wiringpi.pin.WiringPiPin;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 树莓派监控端点
 *
 * @author HouKunLin
 * @date 2020/2/13 0013 2:06
 */
@Component
@Endpoint(id = "raspberry-pi")
public class RaspberryPiEndpoint {

    @ReadOperation
    public Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, Object> physPinStatus = new LinkedHashMap<>();
        // 调用wiringPiSetup函数后的毫秒数
        map.put("millis", WiringPi.millis());
        // 调用wiringPiSetup函数后的微秒数
        map.put("micros", WiringPi.micros());
        map.put("PhysPin-names", getPinValue(PhysPin.values()));
        map.put("WiringPiPin-names", getPinValue(WiringPiPin.values()));
        map.put("BcmPin-names", getPinValue(BcmPin.values()));
        return map;
    }

    public Map<String, Object> getPinValue(IPin[] pins) {
        Map<String, Object> pinStatus = new LinkedHashMap<>();
        for (IPin iPin : pins) {
            int pinId = iPin.pin();
            String key = String.format("%s(%s)", iPin, pinId);
            pinStatus.computeIfAbsent(key, k -> WiringPi.digitalRead(pinId));
        }
        return pinStatus;
    }
}
