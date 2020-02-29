package com.wiringpi.modules.airplane.dto;

import lombok.Data;

/**
 * GPS 信息
 *
 * @author HouKunLin
 * @date 2020/2/21 0021 16:34
 */
@Data
public class Gps {
    /**
     * 高度
     */
    private double height;
    /**
     * 经度
     */
    private double lng;
    /**
     * 纬度
     */
    private double lat;
    /**
     * 速度
     */
    private double speed;
}
