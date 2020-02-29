package com.wiringpi.demo.converter;

import com.wiringpi.pin.modes.IMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.Assert;

/**
 * IMode 类型转换
 *
 * @author HouKunLin
 * @date 2020/2/15 0015 0:22
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class StringToPinModeConverterFactory implements ConverterFactory<String, IMode> {
    private static final Logger logger = LoggerFactory.getLogger(StringToPinModeConverterFactory.class);

    /**
     * @param targetType
     * @return
     * @link org.springframework.core.convert.support.ConversionUtils#getEnumType(java.lang.Class)
     */
    public static Class<?> getEnumType(Class<?> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        Assert.notNull(enumType, () -> "The target type " + targetType.getName() + " does not refer to an enum");
        return enumType;
    }

    @Override
    public <T extends IMode> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnum(getEnumType(targetType));
    }

    private static class StringToEnum<T extends IMode> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            String value = source.trim();
            if (value.isEmpty()) {
                // It's an empty enum identifier: reset the enum value to null.
                return null;
            }
            logger.debug("class type {} value of {}", enumType, source);
            return parsePinMode(value);
        }

        private T parsePinMode(String source) {
            try {
                Integer integer = Integer.valueOf(source);
                return (T) IMode.parseValue(this.enumType, integer);
            } catch (Exception ignore) {
                return (T) IMode.parseValue(this.enumType, source);
            }
        }
    }
}
