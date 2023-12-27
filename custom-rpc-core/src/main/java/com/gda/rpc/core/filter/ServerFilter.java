package com.gda.rpc.core.filter;

import com.gda.rpc.core.common.RpcInvocation;

public interface ServerFilter extends Filter {

    void doFilter(RpcInvocation rpcInvocation);
}
