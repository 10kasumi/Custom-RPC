package com.gda.rpc.core.filter.server;

import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.ServerServiceSemaphoreWrapper;
import com.gda.rpc.core.common.annotations.SPI;
import com.gda.rpc.core.common.excrption.MaxServiceLimitRequestException;
import com.gda.rpc.core.filter.ServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

import static com.gda.rpc.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

@SPI("before")
public class ServerServiceBeforeLimitFilterImpl implements ServerFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerServiceBeforeLimitFilterImpl.class);

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        if(!SERVER_SERVICE_SEMAPHORE_MAP.containsKey(serviceName)){
            return;
        }
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        Semaphore semaphore = serverServiceSemaphoreWrapper.getSemaphore();
        boolean tryRes = semaphore.tryAcquire();
        if(!tryRes){
            LOGGER.error("[ServerServiceBeforeLimitFilterImpl] {}’s max request is {}, reject now",
                    rpcInvocation.getTargetServiceName(), serverServiceSemaphoreWrapper.getMaxNums());
            MaxServiceLimitRequestException rpcException = new MaxServiceLimitRequestException(rpcInvocation);
            rpcInvocation.setE(rpcException);
            throw rpcException;
        }
    }
}
