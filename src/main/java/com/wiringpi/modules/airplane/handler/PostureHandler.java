package com.wiringpi.modules.airplane.handler;

import com.wiringpi.modules.airplane.Airplane;
import com.wiringpi.modules.airplane.enums.Action;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 姿态维持计算
 *
 * @author HouKunLin
 * @date 2020/2/21 0021 17:13
 */
@Component
public class PostureHandler implements Runnable {
    @Autowired
    private Airplane airplane;

    @Override
    public void run() {
        while (true) {
            // Action[] actions = airplane.getActions();
            // 获取姿态信息
            // XYZ轴的姿态信息
            // if (ArrayUtils.contains(actions, Action.NONE)) {
            //     处于悬停状态
            //
            //
            // }
        }
    }

    /**
     * 悬停状态处理
     */
    private void keep() {
        // 如果
    }
}
