package com.wiringpi.modules.camera.flv;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class MetaData {
    private static final Logger logger = LoggerFactory.getLogger(MetaData.class);
    private int afm1Type;
    private int stringLen;
    private char[] afm1String;
    private int afm2Type;
    private Map<Object, Object> map = new LinkedHashMap<>();

    public MetaData(byte[] bytes) {
        afm1Type = Byte.toUnsignedInt(bytes[0]);
        stringLen = (Byte.toUnsignedInt(bytes[1]) << 8) + Byte.toUnsignedInt(bytes[2]);
        afm1String = new char[stringLen];
        for (int i = 0; i < stringLen; i++) {
            afm1String[i] = (char) Byte.toUnsignedInt(bytes[i + 3]);
        }
        afm2Type = Byte.toUnsignedInt(bytes[3 + stringLen]);
        int metaCount = ByteBuffer.wrap(bytes, 1 + 3 + stringLen, 4).getInt();

        int index = 1 + 3 + stringLen + 4;
        for (int i = 0; i < metaCount; i++) {
            Object key;
            Object value;
            int anInt = (Byte.toUnsignedInt(bytes[index]) << 8) + Byte.toUnsignedInt(bytes[index + 1]);
            if (anInt == 0) {
                break;
            }
            // logger.debug("{} 这两个表示长度2个字节: {}", index, anInt);
            index += 2;
            key = getChar(bytes, index, anInt);
            // logger.debug("{} 字符码: {}", index, key);
            index += anInt;
            int valueType = Byte.toUnsignedInt(bytes[index]);
            ++index;
            // logger.debug("{} 间隔了一个间隔符 {}", index, valueType);
            if (valueType == 0x00) {
                // 双精度
                value = ByteBuffer.wrap(bytes, index, 8).getDouble();
                // logger.debug("{} 内容值: {}", index, value);
                index += 8;
            } else if (valueType == 0x01) {
                // stereo 参数是布尔值
                value = bytes[index];
                // logger.debug("stereo = {}", value);
                ++index;
            } else if (valueType == 0x02) {
                // 字符串
                anInt = (Byte.toUnsignedInt(bytes[index]) << 8) + Byte.toUnsignedInt(bytes[index + 1]);
                index += 2;
                try {
                    value = getChar(bytes, index, anInt);
                } catch (Exception e) {
                    // logger.info("遇到错误：key = {},{}，{}，{}", key, index, anInt, Arrays.toString(bytes));
                    throw e;
                }
                // logger.debug("{} 值长度 {}, 值内容： {}", index, anInt, value);
                index += anInt;
            } else {
                // logger.debug("这是一个未知的值类型：{}，key={}", valueType, key);
                continue;
            }
            map.put(key, value);
        }
        Map<Object, Object> newMap = new LinkedHashMap<>();
        newMap.put("duration", map.get("duration"));
        newMap.put("width", map.get("width"));
        newMap.put("height", map.get("height"));
        newMap.put("videodatarate", map.get("videodatarate"));
        newMap.put("framerate", map.get("framerate"));
        newMap.put("videocodecid", map.get("videocodecid"));

        newMap.put("audiodatarate", 125.0);
        newMap.put("audiosamplerate", 44100.0);
        newMap.put("audiosamplesize", 16.0);
        newMap.put("stereo", (byte) 0x01);
        newMap.put("audiocodecid", 10.0);

        newMap.put("encoder", map.get("encoder"));
        newMap.put("filesize", map.get("filesize"));
        map = newMap;
    }

    public static String getChar(byte[] bytes, int offset, int len) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < len; i++) {
            buffer.append((char) Byte.toUnsignedInt(bytes[offset + i]));
        }
        return buffer.toString();
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(afm1Type);
        dataOutputStream.writeByte((stringLen >> 8) & 0xff);
        dataOutputStream.writeByte(stringLen & 0xff);
        for (char c : afm1String) {
            dataOutputStream.writeByte(c);
        }
        dataOutputStream.writeByte(afm2Type);
        dataOutputStream.writeInt(map.size());
        for (Map.Entry<Object, Object> objectEntry : map.entrySet()) {
            Object key = objectEntry.getKey();
            Object value = objectEntry.getValue();
            char[] chars = key.toString().toCharArray();
            dataOutputStream.writeByte((chars.length >> 8) & 0xff);
            dataOutputStream.writeByte(chars.length & 0xff);
            for (char c : chars) {
                dataOutputStream.writeByte(c);
            }
            if (value instanceof Double) {
                dataOutputStream.writeByte(0x00);
                Double aDouble = (Double) value;
                dataOutputStream.writeDouble(aDouble);
            } else if (value instanceof Byte) {
                dataOutputStream.writeByte(0x01);
                dataOutputStream.writeByte((byte) value);
            } else {
                dataOutputStream.writeByte(0x02);
                chars = value.toString().toCharArray();
                dataOutputStream.writeByte((chars.length >> 8) & 0xff);
                dataOutputStream.writeByte(chars.length & 0xff);
                for (char c : chars) {
                    dataOutputStream.writeByte(c);
                }
            }
        }
        dataOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }
}
