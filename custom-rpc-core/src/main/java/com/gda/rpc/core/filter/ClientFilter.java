package com.gda.rpc.core.filter;

import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.common.RpcInvocation;

import java.util.List;

public interface ClientFilter extends Filter{

    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}
