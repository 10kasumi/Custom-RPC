package com.gda.rpc.core.proxy.javassist;

import com.gda.rpc.core.client.RpcReferenceWrapper;
import com.gda.rpc.core.proxy.ProxyFactory;

public class JavassistProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                rpcReferenceWrapper.getAimClass(),
                new JavassistInvocationHandler(rpcReferenceWrapper));
    }
}
