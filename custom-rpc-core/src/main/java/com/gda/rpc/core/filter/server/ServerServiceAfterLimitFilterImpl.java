package com.gda.rpc.core.filter.server;

import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.ServerServiceSemaphoreWrapper;
import com.gda.rpc.core.common.annotations.SPI;
import com.gda.rpc.core.filter.ServerFilter;

import static com.gda.rpc.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

/**
 * 服务端释放semaphore
 */
@SPI("after")
public class ServerServiceAfterLimitFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        if(!SERVER_SERVICE_SEMAPHORE_MAP.containsKey(serviceName)){
            return;
        }
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        serverServiceSemaphoreWrapper.getSemaphore().release();
    }
}
