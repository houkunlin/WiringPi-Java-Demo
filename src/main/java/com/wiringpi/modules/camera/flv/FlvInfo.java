package com.wiringpi.modules.camera.flv;

import com.wiringpi.modules.camera.CustomConsumer;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@Data
public class FlvInfo {
    private static final Logger logger = LoggerFactory.getLogger(FlvInfo.class);
    private byte[] flvHeader;
    private byte[] scriptTag;
    private byte[] info1;
    private byte[] info2;
    private InputStream flvInputStream;
    private ScriptTag scriptTagInfo;

    public FlvInfo(InputStream inputStream) throws IOException {
        flvInputStream = inputStream;
        flvHeader = FlvUtils.readFlvHeader(inputStream);

        getPreviousTagSize();
        scriptTag = FlvUtils.readTag(inputStream);

        if (18 != scriptTag[0]) {
            throw new RuntimeException("不存在 ScriptTag ，请检查视频是否完整");
        }

        getPreviousTagSize();
        info1 = FlvUtils.readTag(inputStream);

        getPreviousTagSize();
        info2 = FlvUtils.readTag(inputStream);
    }

    public byte[] getScriptTag() {
        if (scriptTagInfo != null) {
            try {
                return scriptTagInfo.toBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return scriptTag;
    }

    public ScriptTag getScriptTagInfo() {
        if (scriptTagInfo == null) {
            scriptTagInfo = new ScriptTag(scriptTag);
        }
        return scriptTagInfo;
    }

    public byte[] getPreviousTagSize() throws IOException {
        return FlvUtils.readPreviousTagSize(flvInputStream);
    }

    public byte[] getPreviousTagSize(int previousTagByteLength) {
        return ByteUtils.int2byte(previousTagByteLength);
    }

    @SafeVarargs
    public final void writeTagWithPreviousTag(byte[] tag, int previousTagByteLength, CustomConsumer<byte[]>... consumers) throws Exception {
        byte[] previousTagSize = getPreviousTagSize(previousTagByteLength);
        for (CustomConsumer<byte[]> consumer : consumers) {
            consumer.accept(previousTagSize);
            consumer.accept(tag);
        }
    }

    @SafeVarargs
    public final void writeBytes(byte[] bytes, CustomConsumer<byte[]>... consumers) throws Exception {
        for (CustomConsumer<byte[]> consumer : consumers) {
            consumer.accept(bytes);
        }
    }

    /**
     * 直接发送所有已读取的头部信息，不判断信息是否是视频信息、音频信息
     *
     * @param consumers
     * @throws Exception
     */
    @SafeVarargs
    public final void writeFlvBytes(CustomConsumer<byte[]>... consumers) throws Exception {
        writeBytes(flvHeader, consumers);

        byte[] tag = getScriptTag();
        writeTagWithPreviousTag(tag, 0, consumers);
        logger.debug("info1 是否是视频信息：{}", isVideoHeaderPack(info1));
        logger.debug("info2 是否是音频信息：{}", isAudioHeaderPack(info2));
        writeTagWithPreviousTag(info1, info1.length, consumers);
        writeTagWithPreviousTag(info2, info2.length, consumers);
    }

    /**
     * 发送 FLV 开始的信息字节（包含视频信息、音频信息，自动判断这两个信息，非视频、音频信息则忽略不发烧）
     *
     * @param consumers
     * @throws Exception
     */
    @SafeVarargs
    public final void writeFlvStartInfoBytes(CustomConsumer<byte[]>... consumers) throws Exception {
        writeBytes(flvHeader, consumers);

        byte[] tag = getScriptTag();
        writeTagWithPreviousTag(tag, 0, consumers);
        logger.debug("info1 是否是视频信息：{}", isVideoHeaderPack(info1));
        logger.debug("info2 是否是音频信息：{}", isAudioHeaderPack(info2));
        if (isVideoHeaderPack(info1)) {
            logger.debug("info1 是一个视频头部数据，正在发送补充数据");
            writeTagWithPreviousTag(info1, info1.length, consumers);
        }
        if (isAudioHeaderPack(info2)) {
            logger.debug("info2 是一个音频头部数据，正在发送补充数据");
            writeTagWithPreviousTag(info2, info2.length, consumers);
        }
    }

    public boolean isVideoPack(byte[] bytes) {
        return bytes.length > 12 && bytes[0] == 0x09;
    }

    public boolean isVideoHeaderPack(byte[] bytes) {
        return bytes.length > 12 && bytes[0] == 0x09 && (Byte.toUnsignedInt(bytes[11]) >> 4) == 0x01 && bytes[12] == 0x00;
    }

    public boolean isAudioPack(byte[] bytes) {
        return bytes.length > 12 && bytes[0] == 0x08;
    }

    public boolean isAudioHeaderPack(byte[] bytes) {
        return bytes.length > 12 && bytes[0] == 0x08 && bytes[12] == 0x00;
    }
}
