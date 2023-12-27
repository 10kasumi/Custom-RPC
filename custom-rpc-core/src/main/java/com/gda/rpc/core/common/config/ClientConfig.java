package com.gda.rpc.core.common.config;

public class ClientConfig {
    private String registerAddr;

    private String registerType;

    private String applicationName;

    /**
     * 代理类型：jdk、javassist
     */
    private String proxyType;

    /**
     * random、rotate
     */
    private String routerStrategy;

    /**
     * 客户端序列方式：hessian2, kryo, jdk, fastjson
     */
    private String clientSerialize;

    /**
     * 客户端发送数据的超时时间
     */
    private Integer timeout;

    private Integer maxServerRespDataSize;

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getRouterStrategy() {
        return routerStrategy;
    }

    public void setRouterStrategy(String routerStrategy) {
        this.routerStrategy = routerStrategy;
    }

    public String getClientSerialize() {
        return clientSerialize;
    }

    public void setClientSerialize(String clientSerialize) {
        this.clientSerialize = clientSerialize;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public Integer getTimeOut() {
        return timeout;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeout = timeOut;
    }

    public Integer getMaxServerRespDataSize() {
        return maxServerRespDataSize;
    }

    public void setMaxServerRespDataSize(Integer maxServerRespDataSize) {
        this.maxServerRespDataSize = maxServerRespDataSize;
    }
}
