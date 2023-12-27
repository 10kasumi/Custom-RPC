package com.gda.rpc.core.common.event.listener;

import com.gda.rpc.core.client.ConnectionHandler;
import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.common.event.RpcUpdateEvent;
import com.gda.rpc.core.common.event.data.ProviderNodeInfo;
import com.gda.rpc.core.common.event.data.URLChangeWrapper;
import com.gda.rpc.core.registry.URL;
import com.gda.rpc.core.router.Selector;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gda.rpc.core.common.cache.CommonClientCache.CONNECT_MAP;
import static com.gda.rpc.core.common.cache.CommonClientCache.ROUTER;

public class ServiceUpdateListener implements RpcListener<RpcUpdateEvent>{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUpdateListener.class);

    @Override
    public void callBack(Object o) {
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) o;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());
        List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();
        Set<String> finalUrl = new HashSet<>();
        List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            if (!matchProviderUrl.contains(oldServerAddress)) {
                continue;
            }
            finalChannelFutureWrappers.add(channelFutureWrapper);
            finalUrl.add(oldServerAddress);
        }

        List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();
        for (String newProviderUrl : matchProviderUrl) {
            if(!finalUrl.contains(newProviderUrl)){
                ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                String host = newProviderUrl.split(":")[0];
                Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                channelFutureWrapper.setHost(host);
                channelFutureWrapper.setPort(port);
                String urlStr = urlChangeWrapper.getNodeDataUrl().get(newProviderUrl);
                ProviderNodeInfo providerNodeInfo = URL.buildUrlFromUrlStr(urlStr);
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                channelFutureWrapper.setGroup(providerNodeInfo.getGroup());
                ChannelFuture channelFuture = null;
                try{
                    channelFuture = ConnectionHandler.createChannelFuture(host, port);
                    LOGGER.debug("channelFuture reconnect, server is {}:{}", host, port);
                    channelFutureWrapper.setChannelFuture(channelFuture);
                    newChannelFutureWrapper.add(channelFutureWrapper);
                    finalUrl.add(newProviderUrl);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        finalChannelFutureWrappers.addAll(newChannelFutureWrapper);
        CONNECT_MAP.put(urlChangeWrapper.getServiceName(), finalChannelFutureWrappers);
        Selector selector = new Selector();
        selector.setProviderServiceName(urlChangeWrapper.getServiceName());
        ROUTER.refreshRouterArr(selector);
    }
}
