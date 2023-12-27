package com.gda.rpc.core.proxy.jdk;

import com.gda.rpc.core.client.RpcReferenceWrapper;
import com.gda.rpc.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.gda.rpc.core.common.cache.CommonClientCache.RESP_MAP;
import static com.gda.rpc.core.common.cache.CommonClientCache.SEND_QUEUE;
import static com.gda.rpc.core.common.constants.RpcConstants.DEFAULT_TIMEOUT;

public class JDKClientInvocationHandler implements InvocationHandler {

    private final static Object OBJECT = new Object();

    private int timeOut = DEFAULT_TIMEOUT;

    private final RpcReferenceWrapper<?> rpcReferenceWrapper;

    public JDKClientInvocationHandler(RpcReferenceWrapper<?> rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
        timeOut = Integer.parseInt(rpcReferenceWrapper.getTimeOut());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttachments());
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        rpcInvocation.setRetry(rpcReferenceWrapper.getRetry());

        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        SEND_QUEUE.add(rpcInvocation);
        if(rpcReferenceWrapper.isAsync()){
            return null;
        }
        long beginTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - beginTime < timeOut || rpcInvocation.getRetry() > 0){
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if(object instanceof RpcInvocation){
                RpcInvocation rpcInvocationResp = (RpcInvocation) object;
                if(rpcInvocationResp.getE() != null && rpcInvocationResp.getRetry() > 0){
                    //重新请求
                    rpcInvocation.setE(null);
                    rpcInvocation.setResponse(null);
                    rpcInvocation.setRetry(rpcInvocation.getRetry() - 1);
                    RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
                    SEND_QUEUE.add(rpcInvocation);
                    beginTime = System.currentTimeMillis();
                } else {
                    RESP_MAP.remove(rpcInvocation.getUuid());
                    return rpcInvocationResp.getResponse();
                }
            }
            //超时
            if(System.currentTimeMillis() - beginTime > timeOut){
                rpcInvocation.setResponse(null);
                rpcInvocation.setRetry(rpcInvocation.getRetry() - 1);
                RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
                SEND_QUEUE.add(rpcInvocation);
                beginTime = System.currentTimeMillis();
            }
        }
        RESP_MAP.remove(rpcInvocation.getUuid());
        throw new TimeoutException("client wait server's response timeout!");
    }
}
