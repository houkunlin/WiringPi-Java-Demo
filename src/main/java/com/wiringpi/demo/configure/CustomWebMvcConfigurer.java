package com.wiringpi.demo.configure;

import com.wiringpi.demo.converter.StringToPinModeConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 自定义WebMvc配置
 * <p>
 * 在这里不要加 @EnableWebMvc 注解，因为该注解会使 WebMvcAutoConfiguration 对象的配置失效。
 * 如果想要捕获 NoHandlerFoundException 404 错误，请直接重新继承实现 BasicErrorController 对象的功能，而不是直接在 ControllerAdvice 中捕获
 * </p>
 *
 * @author HouKunLin
 */
@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {
    private final static Logger logger = LoggerFactory.getLogger(CustomWebMvcConfigurer.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .maxAge(3600);
        logger.debug("配置Cors:{}", registry);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToPinModeConverterFactory());
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // https://www.cnblogs.com/gdme1320/p/9391067.html
        // https://www.jianshu.com/p/403fcb340257
        // if (!registry.hasRegistrations()) {
        // 设置一个默认的视图解析器，如果不设置这个，
        // 在使用授权码登录的时候（approved=false）会跳转到 forward:/oauth/confirm_access 页面，
        // 此时会报错 Could not resolve view with name 'forward:/oauth/confirm_access' in servlet with name 'dispatcherServlet
        // registry.jsp();
        // }
        // 2019-12-28 修改
        // 在删除 @EnableWebMvc 注解后，WebMvcAutoConfiguration会为我们定义好一些ViewResolver，在这些ViewResolver中已经包含了jsp的ViewResolver对象
    }

    /**
     * 添加转换器.
     * 说明一下这么做的原因。
     * 想要在 RestControllerAdvice 捕获 NoHandlerFoundException 404 错误，进行统一json格式化输出，
     * 结果会导致 application.properties 配置中的 spring.jackson.date-format 时间格式化失效，
     * 因此，在这里进行重新配置，以便时间格式化生效
     * <p>
     * ## 2019-12-28 修改
     * 删除 @EnableWebMvc 注解，不再捕获 NoHandlerFoundException 404 错误，也就是 spring.mvc.throw-exception-if-no-handler-found 配置不会生效
     * 不生效的原因 L1017 org.springframework.web.servlet.DispatcherServlet#doDispatch(HttpServletRequest, HttpServletResponse)
     * 如果想要再次自定义捕获 404 错误，请直接重新实现 BasicErrorController 对象的功能
     * </p>
     *
     * @param converters 转换器列表
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 添加系统中已有的转换器（自动注入的 List<HttpMessageConverter<?>> converters 列表应该有
        // StringHttpMessageConverter、MappingJackson2HttpMessageConverter ）
        // （在引入了 micrometer-registry-prometheus 依赖来进行返回应用指标数据时是返回文本的，
        // StringHttpMessageConverter 用来解决直接返回文本的问题）
        /*for (HttpMessageConverter<?> converter : this.converters) {
            if (!converters.contains(converter)) {
                converters.add(converter);
            }
        }*/
        // logger.debug("配置HttpMessageConverter转换器:{}", converters);
        //
        // 2019-12-28 修改
        // 把这个方法的代码删除的原因，不再使用 @EnableWebMvc 注解来启用WebMVC配置，因为系统会自动配置 WebMvcAutoConfiguration 对象来进行MVC配置，
        // 如果使用 @EnableWebMvc 注解会导致 DelegatingWebMvcConfiguration 先加入到 ApplicationContext 中，从而导致 WebMvcAutoConfiguration 配置不生效。
        // 由于 WebMvcAutoConfiguration 会为我们配置很多的默认配置，直接开箱急用即可，不必为了捕获 NoHandlerFoundException 404 错误而丢失这些默认配置
        // 如果想要再次自定义捕获 404 错误，请直接重新继承实现 BasicErrorController 对象的功能
    }

    @PostConstruct
    public void postConstruct() {
        logger.debug("自定义Web Mvc配置程序: {}", this);
    }
}
