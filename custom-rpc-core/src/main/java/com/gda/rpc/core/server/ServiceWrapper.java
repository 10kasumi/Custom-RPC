package com.gda.rpc.core.server;

/**
 * 服务端包装类
 */
public class ServiceWrapper {

    /**
     * 对外暴露的具体服务对象
     */
    private Object serviceBean;

    /**
     * 暴露服务的分组
     */
    private String group = "default";

    private String serviceToken;

    /**
     * 限流策略
     */
    private Integer limit;

    private Integer weight = 100;

    public ServiceWrapper(Object serviceBean){
        this.serviceBean = serviceBean;
    }

    public ServiceWrapper(Object serviceObj, String group) {
        this.serviceBean = serviceObj;
        this.group = group;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Object getServiceBean() {
        return serviceBean;
    }

    public void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
