package com.wiringpi.modules.camera.dto;

import lombok.Data;

/**
 * ffmpeg 启动参数
 *
 * @author HouKunLin
 * @date 2020/2/16 0016 14:46
 */
@Data
public class FfmpegDTO {
    private Integer r = 25;
    private String s = "640x480";
    private String bv = "512k";
    private String cv = "h264_omx";
    private String audio = "/home/pi/mp3.mp3";
    private String ca = "aac";
    private String strict = "experimental";
    private String vf = "\"settb=AVTB,setpts='trunc(PTS/1K)*1K+st(1,trunc(RTCTIME/1K))-1K*trunc(ld(1)/1K)',drawtext=fontsize=20:fontcolor=black:text='%{localtime}.%{eif\\:1M*t-1K*trunc(t*1K)\\:d}':box=enable:boxcolor=white\"";
    private String f = "flv";
    private String url = "http://127.0.0.1:8080/raspberry-pi/camera/push";

    public String[] toCommand() {
        return new String[]{
                "ffmpeg",
                "-r", String.valueOf(r),
                "-s", s,
                "-i", "/dev/video0",
                // "-i", audio,
                "-b:v", bv,
                "-c:v", cv,
                // "-c:a", ca,
                // "-strict", strict,
                // "-vf", vf,
                "-f", f, url
        };
    }
}
