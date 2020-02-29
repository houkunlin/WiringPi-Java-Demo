# WiringPi-Java-Demo

这是一个`SpringBoot`应用，以SpringBoot框架为基础来开发，可以通过对外暴露API来控制树莓派的引脚信息，也可以使用SpringBoot整合WebSocket来进行一些实时性的操作。



- `com.wiringpi.demo.listener.StartupEventListener` 在应用启动时启动WiringPi设置
- `com.wiringpi.demo.listener.ShutdownEventListener` 在应用关闭之前做一些清理工作
- `com.wiringpi.demo.endpoint.RaspberryPiEndpoint` 对外提供一些监控类型数据
- `com.wiringpi.demo.converter.StringToPinModeConverterFactory` 对 `WiringPi-Java` 的 `com.wiringpi.pin.modes.IMode` 进行转换操作



