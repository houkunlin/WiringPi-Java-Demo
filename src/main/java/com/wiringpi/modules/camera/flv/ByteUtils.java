package com.wiringpi.modules.camera.flv;

import java.io.ByteArrayOutputStream;

public class ByteUtils {

    public static int byte2int(byte... bytes) {
        int num = 0;
        for (int i = 0; i < bytes.length; i++) {
            num += Byte.toUnsignedInt(bytes[i]) << (bytes.length - i - 1) * 8;
        }
        return num;
    }

    public static long byte2long(byte... bytes) {
        long num = 0;
        for (int i = 0; i < bytes.length; i++) {
            num += Byte.toUnsignedLong(bytes[i]) << (bytes.length - i - 1) * 8;
            // long unsignedLong = Byte.toUnsignedLong(bytes[i]);
            // int leftBit = (bytes.length - i - 1) * 8;
            // long leftResult = unsignedLong << leftBit;
            // num += leftResult;
            // logger.debug("byte = {} , long = {}, bit << {} = {}", bytes[i], unsignedLong, ((bytes.length - i - 1) * 8), leftResult);
        }
        return num;
    }

    public static double byte2double(byte... bytes) {
        return Double.longBitsToDouble(byte2long(bytes));
    }

    public static byte[] int2byte(int number) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int i = 0; i < 4; i++) {
            long l = (number >> (4 - i - 1) * 8) & 0xff;
            bytes.write((int) l);
        }
        return bytes.toByteArray();
    }

    public static byte[] long2byte(long number) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int i = 0; i < 8; i++) {
            long l = (number >> (8 - i - 1) * 8) & 0xff;
            bytes.write((int) l);
        }
        return bytes.toByteArray();
    }

    public static byte[] double2byte(Double number) {
        return long2byte(Double.doubleToLongBits(number));
    }
}
