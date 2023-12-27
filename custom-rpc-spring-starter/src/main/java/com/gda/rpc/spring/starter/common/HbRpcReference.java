package com.gda.rpc.spring.starter.common;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HbRpcReference {

    /**
     * 连接url
     * @return
     */
    String url() default "";

    /**
     * 服务分组
     * @return
     */
    String group() default "default";

    /**
     * 服务令牌
     * @return
     */
    String serviceToken() default "";

    int timeout() default 3000;

    int retry() default 1;

    /**
     * 是否使用异步调用
     * @return
     */
    boolean async() default false;
}
