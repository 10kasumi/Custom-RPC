package com.gda.rpc.spring.starter.common;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface HbRpcService {

    /**
     * 限流
     */
    int limit() default 0;

    /**
     * 服务权重
     * @return
     */
    int weight() default 100;

    /**
     * 服务分组
     */
    String group() default "default";

    /**
     * 服务令牌
     */
    String serviceToken() default "";
}
