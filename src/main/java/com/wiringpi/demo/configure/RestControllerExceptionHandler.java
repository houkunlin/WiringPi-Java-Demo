package com.wiringpi.demo.configure;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HouKunLin
 */
@RestControllerAdvice
public class RestControllerExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestControllerExceptionHandler.class);
    private final HttpServletRequest request;

    public RestControllerExceptionHandler(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 严重的错误，不在 @ExceptionHandler 捕获名单里面的错误
     *
     * @param e 错误
     * @return json
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> exception(Throwable e) {
        logger.error("严重错误，从未考虑到的错误范围", e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(getMap(e, status), status);
    }

    /**
     * 其他未考虑到的所有错误
     *
     * @param e 错误
     * @return json
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exception(Exception e) {
        logger.error("严重错误，未捕获的其他异常", e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(getMap(e, status), status);
    }

    /**
     * 客户端中止异常
     *
     * @param e 错误
     * @return json
     */
    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<?> clientAbortException(ClientAbortException e) {
        logger.error("{} {}?{} 客户端中止异常: {}", request.getMethod(), request.getRequestURI(), request.getQueryString(), e.getMessage());
        return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 空指针错误
     *
     * @param e 错误
     * @return json
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> nullPointerException(NullPointerException e) {
        logger.error("空指针错误", e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(getMap(e, status), status);
    }

    /**
     * Http请求方法不支持异常，请求一个未定义的 HttpMethod 方法。
     * 例如：
     * <p>定义了 @GetMapping("/user") ，但是使用了 POST、PUT、DELETE 请求了该 URI ，则抛出该异常</p>
     *
     * @param e 错误
     * @return json
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<?> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.error("Http请求方法不支持异常", e);
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        List<String> message = new ArrayList<>();
        message.add(String.format("URI不支持 %s 请求", e.getMethod()));
        Set<HttpMethod> supportedHttpMethods = e.getSupportedHttpMethods();
        if (supportedHttpMethods != null) {
            String supportedMethods = supportedHttpMethods.stream().map(Enum::name).collect(Collectors.joining("/"));
            message.add(String.format("该URI可能支持 %s 请求", supportedMethods));
        }
        return new ResponseEntity<>(getMap(String.join(", ", message), status), status);
    }

    /**
     * WEB 404 错误，不启用 @EnableWebMvc 注解， spring.mvc.throw-exception-if-no-handler-found 配置失效，无法抛出404异常在这里捕获处理。
     * 如果要捕获404错误，请重新继承实现 BasicErrorController 功能
     *
     * @param e 错误
     * @return json
     */
    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<?> noHandlerFoundException(NoHandlerFoundException e) {
        logger.error("404错误", e);
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(getMap(e, status), status);
    }

    /**
     * WEB 请求类型转换错误，请求的数据类型转换错误异常
     *
     * @param e 错误
     * @return json
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<?> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error("数据类型转换错误: {}", e.getLocalizedMessage());
        logger.error("数据类型转换错误", e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(getMap(e, status), status);
    }

    /**
     * WEB 请求数据校验错误。使用 @Valid 和 @Validated 校验请求参数数据出现错误（检验不合格）时抛出这个异常（手动抛出）
     *
     * @param e 错误
     * @return json
     */
    @ExceptionHandler({BindException.class})
    public ResponseEntity<?> bindException(BindException e) {
        logger.error("请求参数数据校验不通过", e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = e.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(getMap(message, status), status);
    }

    private Map<String, Object> getMap(Throwable throwable, HttpStatus status) {
        return getMap(throwable.getMessage(), status);
    }

    private Map<String, Object> getMap(String msg, HttpStatus status) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", status.value());
        map.put("msg", msg);
        map.put("uri", request.getRequestURI());
        map.put("timestamp", new Date());
        return map;
    }
}
