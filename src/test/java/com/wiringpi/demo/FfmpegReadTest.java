package com.wiringpi.demo;


import com.wiringpi.modules.camera.flv.FlvInfo;
import com.wiringpi.modules.camera.flv.ScriptTag;
import com.wiringpi.modules.camera.flv.ByteUtils;
import com.wiringpi.modules.camera.flv.FlvUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

public class FfmpegReadTest {
    private static final Logger logger = LoggerFactory.getLogger(FfmpegReadTest.class);

    @Test
    public void test() {
        System.out.println("FlvHeader : " + Arrays.toString(new byte[]{0x46, 0x4c, 0x56, 0x01, 0x05, 0x09}));
        byte[] bytes;
        bytes = new byte[]{0x40, 0x35, 0x28, (byte) 0xf5, (byte) 0xc2, (byte) 0x8f, 0x5c, 0x29};
        System.out.println("原始二进制信息");
        out(bytes);
        System.out.println("字节转Long：" + ByteUtils.byte2long(bytes));
        System.out.println("字节转Double：" + ByteUtils.byte2double(bytes));
        System.out.println("Long转字节");
        out(ByteUtils.long2byte(ByteUtils.byte2long(bytes)));
        System.out.println("Double转字节");
        out(ByteUtils.double2byte(ByteUtils.byte2double(bytes)));
    }

    @Test
    public void testReadFlv_v0() throws Exception {
        FileInputStream inputStream;
        inputStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\test.flv");
        logger.debug("FLV 标头");
        out(FlvUtils.readFlvHeader(inputStream));
        for (int i = 0; i < 100; i++) {
            FlvUtils.readPreviousTagSize(inputStream);
            byte[] pack = FlvUtils.readTag(inputStream);
            if (18 == pack[0]) {
                System.out.println("原始包");
                out(pack);
                ScriptTag scriptTag = new ScriptTag(pack);
                System.out.println("解析重构包");
                out(scriptTag.toBytes());
                System.out.println(scriptTag);
                scriptTag.setDuration(Double.MAX_VALUE);
                scriptTag.setFilesize(Double.MAX_VALUE);
                System.out.println(scriptTag);
                System.out.println("解析改写内容重构包");
                out(scriptTag.toBytes());
            } else {
                break;
            }
        }
    }

    @Test
    public void testCopyFlv_v1() throws Exception {
        FileInputStream inputStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\flv.flv");
        FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\flv.01.flv");
        byte[] flvTag = FlvUtils.readFlvHeader(inputStream);
        outputStream.write(flvTag);
        int index = 0;
        int lastTagSize = 0;
        do {
            if (FlvUtils.readPreviousTagSize(inputStream).length == 0) {
                break;
            }
            byte[] pack = FlvUtils.readTag(inputStream);

            if (pack.length == 0) {
                break;
            }
            if (18 == pack[0]) {
                ScriptTag scriptTag = new ScriptTag(pack);
                scriptTag.setDuration((double) Integer.MAX_VALUE);
                scriptTag.setFilesize((double) Integer.MAX_VALUE);
                pack = scriptTag.toBytes();
            }
            outputStream.write(ByteUtils.int2byte(lastTagSize));
            outputStream.write(pack);
            lastTagSize = pack.length;
            if (index % 50 == 0) {
                logger.debug("读取数据片段：{}, 类型: {} ", index, pack[0] & 0b11111);
            }
            ++index;
        } while (true);
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    @Test
    public void testCopyFlv_v2() throws Exception {
        FileInputStream inputStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\test.flv");
        FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\test.02.flv");
        FlvInfo flvInfo = new FlvInfo(inputStream);
        flvInfo.writeFlvBytes(outputStream::write);

        int len;
        byte[] bytes = new byte[4096];
        while ((len = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }

    public static void out(byte[] bytes) {
        out(bytes, 0, bytes.length);
    }

    public static void out(byte[] bytes, int offset, int len) {
        // logger.debug("bytes len = {} , start {} , end {}", bytes.length, offset, len);
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                if (i % 16 == 0) {
                    System.out.println();
                } else if (i % 2 == 0) {
                    System.out.print(" ");
                }
            }
            String string = Integer.toString(Byte.toUnsignedInt(bytes[offset + i]), 16);
            if (string.length() < 2) {
                System.out.print("0");
            }
            System.out.print(string);
        }
        System.out.println();
        System.out.println();
    }

}
