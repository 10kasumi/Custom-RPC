package com.gda.rpc.core.filter.client;

import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.filter.ClientFilter;

import java.util.ArrayList;
import java.util.List;

public class ClientFilterChain {

    private static List<ClientFilter> clientFilterList = new ArrayList<>();

    public void addClientFilter(ClientFilter clientFilter){
        clientFilterList.add(clientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation){
        for(ClientFilter clientFilter : clientFilterList){
            clientFilter.doFilter(src, rpcInvocation);
        }
    }
}
