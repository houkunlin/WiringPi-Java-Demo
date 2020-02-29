package com.wiringpi.modules.camera.flv;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

@Data
public class ScriptTag {
    private static final Logger logger = LoggerFactory.getLogger(ScriptTag.class);
    private byte[] header;
    private byte[] body;
    private MetaData metaData;

    public ScriptTag(byte[] bytes) {
        // TagHeader 0-11
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i < 11; i++) {
            byteArrayOutputStream.write(bytes[i]);
        }
        header = byteArrayOutputStream.toByteArray();

        // 11 之后为 TagBody，这里是 MetaData 信息
        byteArrayOutputStream.reset();
        for (int i = 11; i < bytes.length; i++) {
            byteArrayOutputStream.write(bytes[i]);
        }
        body = byteArrayOutputStream.toByteArray();
        metaData = new MetaData(body);

        // 完整的 Bytes 字节数组解析过程

        // out(bytes);
        // logger.debug("TagHeader");
        // out(bytes, 0, 11);
        // logger.debug("第一个AMF");
        // out(bytes, 11, 13);
        // logger.debug("第二个AMF");
        // out(bytes, 24, 5);
        // int index = 29;
        // while (index <= bytes.length - 1) {
        //     int anInt = (Byte.toUnsignedInt(bytes[index]) << 8) + Byte.toUnsignedInt(bytes[index + 1]);
        //     if (anInt == 0) {
        //         break;
        //     }
        //     logger.debug("{} 这两个表示长度2个字节: {}", index, anInt);
        //     index += 2;
        //     logger.debug("{} 字符码: {}", index, getChar(bytes, index, anInt));
        //     index += anInt;
        //     logger.debug("{} 间隔了一个 0x00 = {}", index, Byte.toUnsignedInt(bytes[index]));
        //     if (Byte.toUnsignedInt(bytes[index]) == 0x00) {
        //         ++index;
        //         logger.debug("{} 内容值: {}", index, ByteBuffer.wrap(bytes, index, 8).getDouble());
        //         index += 8;
        //     } else {
        //         ++index;
        //         anInt = (Byte.toUnsignedInt(bytes[index]) << 8) + Byte.toUnsignedInt(bytes[index + 1]);
        //         index += 2;
        //         logger.debug("{} 值长度 {}, 值内容： {}", index, anInt, getChar(bytes, index, anInt));
        //         index += anInt;
        //     }
        // }
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        byte[] bytes = metaData.toBytes();
        int dataSize = bytes.length + 3;
        // 重写 TagHeader 的 DataSize 参数
        header[1] = (byte) ((dataSize >> 16) & 0xff);
        header[2] = (byte) ((dataSize >> 8) & 0xff);
        header[3] = (byte) (dataSize & 0xff);
        byteArrayOutputStream.write(header);
        byteArrayOutputStream.write(bytes);
        byteArrayOutputStream.write(new byte[]{0x00, 0x00, 0x09});
        // logger.debug("bytes.length = {}, body.length = {}", bytes.length, body.length);
        // byteArrayOutputStream.write(body, bytes.length, body.length - bytes.length);
        return byteArrayOutputStream.toByteArray();
    }

    public void setDuration(Double duration) {
        Map<Object, Object> map = metaData.getMap();
        map.put("duration", duration);
    }

    public void setFilesize(Double duration) {
        Map<Object, Object> map = metaData.getMap();
        map.put("filesize", duration);
    }
}
