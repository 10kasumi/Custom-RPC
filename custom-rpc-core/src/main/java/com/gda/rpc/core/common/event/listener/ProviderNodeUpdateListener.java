package com.gda.rpc.core.common.event.listener;

import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.common.event.RpcNodeUpdateEvent;
import com.gda.rpc.core.common.event.data.ProviderNodeInfo;
import com.gda.rpc.core.registry.URL;

import java.util.List;

import static com.gda.rpc.core.common.cache.CommonClientCache.CONNECT_MAP;
import static com.gda.rpc.core.common.cache.CommonClientCache.ROUTER;

public class ProviderNodeUpdateListener implements RpcListener<RpcNodeUpdateEvent>{
    @Override
    public void callBack(Object o) {
        ProviderNodeInfo providerNodeInfo = (ProviderNodeInfo) o;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerNodeInfo.getServiceName());
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String address = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            if(address.equals(providerNodeInfo.getAddress())){
                //修改权重
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                URL url = new URL();
                url.setServiceName(providerNodeInfo.getServiceName());
                ROUTER.updateWight(url);
                break;
            }
        }
    }
}
