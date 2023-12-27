package com.gda.rpc.core.registry;

import java.util.List;
import java.util.Map;

import static com.gda.rpc.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static com.gda.rpc.core.common.cache.CommonServerCache.PROVIDER_URL_SET;

public abstract class AbstractRegister implements RegistryService{

    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url);
    }

    @Override
    public void doUnSubscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url);
    }

    public abstract void doBeforeSubscribe(URL url);

    public abstract void doAfterSubscribe(URL url);

    /**
     * 获取服务提供者的ip
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderIps(String serviceName);

    /**
     * 获取服务的权重
     * @param serviceName
     * @return <ip:port, urlString>
     */
    public abstract Map<String, String> getServiceWeightMap(String serviceName);
}
