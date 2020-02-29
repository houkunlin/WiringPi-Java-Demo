package com.wiringpi.modules.camera;

@FunctionalInterface
public interface CustomConsumer<T> {
    void accept(T object) throws Exception;
}
