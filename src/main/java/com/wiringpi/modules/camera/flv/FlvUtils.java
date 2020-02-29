package com.wiringpi.modules.camera.flv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FlvUtils {
    /**
     * 读取一个数据包
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readTag(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int tagType = inputStream.read();
        if (tagType == -1) {
            return new byte[0];
        }
        byteArrayOutputStream.write(tagType);
        byte[] bytesDataSize = readInputStreamBytes(inputStream, 3);
        byteArrayOutputStream.write(bytesDataSize);
        int dataSize = ByteUtils.byte2int(bytesDataSize);
        // 这个 7 = Timestamp3 + TimestampExtended1 + StreamID3
        byte[] bytesTimestamp = readInputStreamBytes(inputStream, 3);
        int timestampExt = inputStream.read();
        byte[] bytesStreamId = readInputStreamBytes(inputStream, 3);
        /*logger.debug("数据包长度：{}，TagType类型：{}, 时间戳：{}，扩展时间：{}，序列号：{}",
                dataSize,
                tagType & 0b11111,
                ByteUtils.byte2int(bytesTimestamp),
                timestampExt,
                ByteUtils.byte2int(bytesStreamId));*/

        byteArrayOutputStream.write(bytesTimestamp);
        byteArrayOutputStream.write(timestampExt);
        byteArrayOutputStream.write(bytesStreamId);
        byteArrayOutputStream.write(readInputStreamBytes(inputStream, dataSize));

        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] readFlvHeader(InputStream inputStream) throws IOException {
        return readInputStreamBytes(inputStream, 9);
    }

    public static byte[] readPreviousTagSize(InputStream inputStream) throws IOException {
        return readInputStreamBytes(inputStream, 4);
    }

    /**
     * 读取指定字节数
     *
     * @param inputStream
     * @param len
     * @return
     * @throws IOException
     */
    public static byte[] readInputStreamBytes(InputStream inputStream, int len) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i < len; i++) {
            int read = inputStream.read();
            byteArrayOutputStream.write(read);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
