package com.wiringpi.modules.airplane.dto;

import lombok.Data;

/**
 * 操作请求数据
 *
 * @author HouKunLin
 * @date 2020/2/21 0021 16:49
 */
@Data
public class DirectionDTO {
    /**
     * 垂直参数，应当有一个限制范围（0.0 到 1.0）
     * 其值为-0.5时表示电机未进行初始化操作，此时需要设置此时为1，听到电机提示声后再设置此值为0,
     */
    private double vertical = -0.5;
    /**
     * 左右行进参数，应当有一个限制范围（左-0.2 到 +0.2右）
     */
    private double horizontal = 0;
    /**
     * 前后行进参数，应当有一个限制范围（后-0.2 到 +0.2前）
     */
    private double forwardBackward = 0;
    /**
     * 原地旋转参数，应当有一个限制范围（左旋转-0.1 到 +0.1右旋转）
     */
    private double rotate = 0;

    public void refresh(DirectionDTO dto) {
        this.vertical = dto.vertical;
        this.horizontal = dto.horizontal;
        this.forwardBackward = dto.forwardBackward;
        this.rotate = dto.rotate;
    }

    public void reset() {
        this.vertical = -0.5;
        this.horizontal = 0;
        this.forwardBackward = 0;
        this.rotate = 0;
    }
}
