package com.wiringpi.modules.airplane;

import com.wiringpi.modules.airplane.dto.Power;
import com.wiringpi.pin.BcmPin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 飞机模块配置
 *
 * @author HouKunLin
 * @date 2020/2/21 0021 16:55
 */
@Configuration
public class ConfigPlane {
    /**
     * 飞机对象
     *
     * @return
     */
    @Bean
    public Airplane plane() {
        return new Airplane(BcmPin.p18.gpio(), BcmPin.p19.gpio(), BcmPin.p20.gpio(), BcmPin.p21.gpio());
    }

    /**
     * 飞机电源对象
     *
     * @return
     */
    @Bean
    public Power power() {
        return new Power(BcmPin.p17.gpio());
    }


}
