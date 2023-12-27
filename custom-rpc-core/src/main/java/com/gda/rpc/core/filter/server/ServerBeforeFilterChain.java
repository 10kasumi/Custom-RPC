package com.gda.rpc.core.filter.server;

import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.filter.ServerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端的前置过滤器链
 */
public class ServerBeforeFilterChain {

    private static List<ServerFilter> serverFilters = new ArrayList<>();

    public void addServerFilter(ServerFilter serverFilter){
        serverFilters.add(serverFilter);
    }

    public void doFilter(RpcInvocation rpcInvocation){
        for (ServerFilter serverFilter : serverFilters) {
            serverFilter.doFilter(rpcInvocation);
        }
    }
}
