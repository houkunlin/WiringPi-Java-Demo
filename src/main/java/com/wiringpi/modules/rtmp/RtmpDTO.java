package com.wiringpi.modules.rtmp;

import lombok.Data;

/**
 * RTMP 回调
 *
 * @author HouKunLin
 * @date 2020/2/15 0015 19:08
 */
@Data
public class RtmpDTO {
    private String app;
    private String flashver;
    private String swfurl;
    private String tcurl;
    private String pageurl;
    private String addr;
    private Integer clientid;
    private String call;
    private Long time;
    private Long timestamp;
    private String name;
}
