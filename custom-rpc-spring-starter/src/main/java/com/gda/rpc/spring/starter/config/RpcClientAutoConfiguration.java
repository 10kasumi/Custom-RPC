package com.gda.rpc.spring.starter.config;

import com.gda.rpc.core.client.Client;
import com.gda.rpc.core.client.RpcReference;
import com.gda.rpc.core.client.RpcReferenceWrapper;
import com.gda.rpc.spring.starter.common.HbRpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Field;

import static com.gda.rpc.core.common.cache.CommonClientCache.CLIENT_CONFIG;

public class RpcClientAutoConfiguration implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {

    private static RpcReference rpcReference = null;

    private static Client client = null;

    private volatile boolean needInitClient = false;

    private volatile boolean hasInitClientConfig = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientAutoConfiguration.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if(needInitClient && client != null){
            LOGGER.info("============== [{}] started success ", CLIENT_CONFIG.getApplicationName());
            client.doConnectServer();
            client.startClient();
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for(Field field : fields){
            if(field.isAnnotationPresent(HbRpcReference.class)){
                if(!hasInitClientConfig){
                    client = new Client();
                    try{
                        client.initClientConfig();
                        rpcReference = client.initClientApplication();
                    } catch(Exception e){
                        LOGGER.error("[RpcClientAutoConfiguration] postProcessAfterInitialization has error ", e);
                        throw new RuntimeException(e);
                    }
                    hasInitClientConfig = true;
                }
                needInitClient = true;
                HbRpcReference hbRpcReference = field.getAnnotation(HbRpcReference.class);
                try{
                    field.setAccessible(true);
                    Object refObj = field.get(bean);
                    RpcReferenceWrapper rpcReferenceWrapper = new RpcReferenceWrapper();
                    rpcReferenceWrapper.setAimClass(field.getType());
                    rpcReferenceWrapper.setGroup(hbRpcReference.group());
                    rpcReferenceWrapper.setServiceToken(hbRpcReference.serviceToken());
                    rpcReferenceWrapper.setUrl(hbRpcReference.url());
                    rpcReferenceWrapper.setTimeOut(hbRpcReference.timeout());
                    rpcReferenceWrapper.setRetry(hbRpcReference.retry());
                    rpcReferenceWrapper.setAsync(hbRpcReference.async());
                    refObj = rpcReference.get(rpcReferenceWrapper);
                    field.set(bean, refObj);
                    client.doSubscribeService(field.getType());
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }
        return bean;
    }
}
