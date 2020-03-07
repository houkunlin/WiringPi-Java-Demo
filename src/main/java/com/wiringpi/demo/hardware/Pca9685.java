package com.wiringpi.demo.hardware;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wiringpi.jni.WiringPi;
import com.wiringpi.jni.WiringPiI2C;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PCA9685 这是一个 PWM 扩展板，提供16路 PWM 模拟
 *
 * @author HouKunLin
 * @date 2020/3/5 0005 16:03
 */
@Getter
public class Pca9685 {
    private static final Logger logger = LoggerFactory.getLogger(Pca9685.class);
    private final boolean isDebug = logger.isDebugEnabled();
    private static final int MODE1 = 0x00;
    private static final int MODE2 = 0x01;
    private static final int SUBADR1 = 0x02;
    private static final int SUBADR2 = 0x03;
    private static final int SUBADR3 = 0x04;
    private static final int PRESCALE = 0xFE;
    private static final int LED0_ON_L = 0x06;
    private static final int LED0_ON_H = 0x07;
    private static final int LED0_OFF_L = 0x08;
    private static final int LED0_OFF_H = 0x09;
    private static final int ALLLED_ON_L = 0xFA;
    private static final int ALLLED_ON_H = 0xFB;
    private static final int ALLLED_OFF_L = 0xFC;
    private static final int ALLLED_OFF_H = 0xFD;

    @JsonIgnore
    private WiringPiI2C piI2C;

    /**
     * 默认地址应该是 0x40
     *
     * @param address 设备地址
     */
    public Pca9685(int address) {
        this.piI2C = new WiringPiI2C(address);
        logger.info("Reseting PCA9685");
        this.piI2C.wiringPiI2CWriteReg8(MODE1, 0x00);
    }

    private String hex(int i) {
        return Integer.toHexString(i);
    }

    public void write(int reg, int value) {
        //将8位值写入指定的寄存器/地址
        this.piI2C.wiringPiI2CWriteReg8(reg, value);
        if (isDebug) {
            logger.debug("I2C: Device 0x{} Write 0x{} to register 0x{}", hex(piI2C.getDevId()), hex(value), hex(reg));
        }
    }

    public int read(int reg) {
        // 从I2C设备读取无符号字节
        int result = piI2C.wiringPiI2CReadReg8(reg);
        if (isDebug) {
            logger.debug("I2C: Device 0x{} returned 0x{} from reg 0x{}", hex(piI2C.getDevId()), hex(result & 0xFF), hex(reg));
        }
        return result;
    }

    /**
     * 读取2个寄存器的值，并转换为整型值
     *
     * @param reg 起始寄存器地址
     * @return 返回2个寄存器的值
     */
    public int read2reg(int reg) {
        int value;
        int value1 = piI2C.wiringPiI2CReadReg8(reg + 1);
        int value2 = piI2C.wiringPiI2CReadReg8(reg);
        value = (value1 << 8) + value2;
        if (value >= 0x8000) {
            value = -(65536 - value);
        }
        if (isDebug) {
            logger.debug("I2C: Device 0x{} returned 0x{}+0x{} from reg 0x{}", hex(piI2C.getDevId()), hex(reg), hex(reg + 1), value);
        }
        return value;
    }

    /**
     * 读取指定通道的PWM值
     *
     * @param channel 通道
     * @return 返回指定通道的PWM高电平时间（微秒）
     */
    public int readPwm(int channel) {
        return readPwm(LED0_OFF_L, channel);
    }

    /**
     * 读取指定通道的PWM值
     *
     * @param reg     起始存储器地址
     * @param channel 通道
     * @return 返回指定通道的PWM高电平时间（微秒）
     */
    public int readPwm(int reg, int channel) {
        return read2reg(reg + 4 * channel) * 20000 / 4096;
    }

    /**
     * 设置频率
     *
     * @param freq 频率
     */
    public void setPWMFreq(int freq) {
        // 设定PWM频率
        // 25MHz
        double presCaleVal = 25000000.0;
        // 12-bit
        presCaleVal /= 4096.0;
        presCaleVal /= freq;
        presCaleVal -= 1.0;
        logger.info("将PWM频率设置为 {} Hz", freq);
        logger.info("Estimated pre-scale: {}", presCaleVal);
        double presCale = Math.floor(presCaleVal + 0.5);
        logger.info("Final pre-scale: {}", presCale);

        int oldMode = read(MODE1);
        // #sleep;
        int newMode = (oldMode & 0x7F) | 0x10;
        // go to sleep
        write(MODE1, newMode);
        write(PRESCALE, (int) Math.floor(presCale));
        write(MODE1, oldMode);
        WiringPi.delay(5);
        write(MODE1, oldMode | 0x80);
    }

    public void setPWM(int channel, int on, int off) {
        // 设置单个PWM通道
        write(LED0_ON_L + 4 * channel, on & 0xFF);
        write(LED0_ON_H + 4 * channel, on >> 8);
        write(LED0_OFF_L + 4 * channel, off & 0xFF);
        write(LED0_OFF_H + 4 * channel, off >> 8);
        logger.debug("channel: {}  LED_ON: {} LED_OFF: {}", channel, on, off);
    }

    /**
     * 设置伺服脉冲，PWM频率必须为50HZ
     *
     * @param channel 隧道
     * @param pulse   高电平微秒数
     */
    public void setServoPulse(int channel, int pulse) {
        // 设置伺服脉冲，PWM频率必须为50HZ
        // PWM频率为50HZ，周期为20000us
        logger.debug("channel {} 高电平时间: {} us", channel, pulse);
        pulse = pulse * 4096 / 20000;
        setPWM(channel, 0, pulse);
        logger.debug("");
    }
}
