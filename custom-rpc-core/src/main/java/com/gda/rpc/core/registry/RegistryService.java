package com.gda.rpc.core.registry;

//注册中心
public interface RegistryService {

    /**
     * 注册url
     */
    void register(URL url);

    /**
     * 服务下线
     */
    void unRegister(URL url);

    /**
     * 消费方订阅服务
     */
    void subscribe(URL url);

    /**
     * 取消订阅
     */
    void doUnSubscribe(URL url);
}
