package com.gda.rpc.spring.starter.config;

import com.gda.rpc.core.server.Server;
import com.gda.rpc.core.server.ServerShutdownHook;
import com.gda.rpc.core.server.ServiceWrapper;
import com.gda.rpc.spring.starter.common.HbRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

import static com.gda.rpc.core.common.cache.CommonServerCache.SERVER_CONFIG;

public class RpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerAutoConfiguration.class);

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Server server = null;
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(HbRpcService.class);
        if(beanMap.size() == 0){
            return;
        }

        printBanner();
        long begin= System.currentTimeMillis();
        server = new Server();
        server.initServerConfig();
        for (String beanName : beanMap.keySet()) {
            Object bean = beanMap.get(beanName);
            HbRpcService rpcService = bean.getClass().getAnnotation(HbRpcService.class);
            ServiceWrapper serviceWrapper = new ServiceWrapper(bean, rpcService.group());
            serviceWrapper.setLimit(rpcService.limit());
            serviceWrapper.setServiceToken(rpcService.serviceToken());
            serviceWrapper.setWeight(rpcService.weight());
            server.registryService(serviceWrapper);
            LOGGER.info("=============> [hb-rpc] {} export success! ", beanName);
        }
        ServerShutdownHook.registryShutdownHook();;
        server.startServerApplication();
        long end = System.currentTimeMillis();
        LOGGER.info("=================== [{}] started success in {}s",
                SERVER_CONFIG.getApplicationName(), ((double) end - (double) begin) / 1000);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void printBanner(){
        System.out.println();
        System.out.println("=================================");
        System.out.println("--------HbRPCStarter Now---------");
        System.out.println("=================================");
        System.out.println("version:1.0.0");
        System.out.println();
    }
}
