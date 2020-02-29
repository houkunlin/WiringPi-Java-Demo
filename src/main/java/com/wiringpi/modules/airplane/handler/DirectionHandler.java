package com.wiringpi.modules.airplane.handler;

import com.wiringpi.modules.airplane.Airplane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 方向处理
 *
 * @author HouKunLin
 * @date 2020/2/21 0021 16:54
 */
@Component
public class DirectionHandler implements Runnable {
    @Autowired
    private Airplane airplane;

    @Override
    public void run() {
    }
}
