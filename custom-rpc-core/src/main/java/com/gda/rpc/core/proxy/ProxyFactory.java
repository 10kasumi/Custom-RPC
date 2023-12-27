package com.gda.rpc.core.proxy;

import com.gda.rpc.core.client.RpcReferenceWrapper;

public interface ProxyFactory {
    <T> T getProxy(final RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable;
}
