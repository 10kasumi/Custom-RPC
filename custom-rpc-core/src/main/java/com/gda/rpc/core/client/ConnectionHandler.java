package com.gda.rpc.core.client;

import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.event.data.ProviderNodeInfo;
import com.gda.rpc.core.common.utils.CommonUtil;
import com.gda.rpc.core.registry.URL;
import com.gda.rpc.core.router.Selector;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.gda.rpc.core.common.cache.CommonClientCache.*;

/**
 * 负责连接建立、断开
 */
public class ConnectionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);

    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap){
        ConnectionHandler.bootstrap = bootstrap;
    }

    public static void connect(String providerServiceName, String providerIP) throws InterruptedException{
        if(bootstrap == null){
            throw new RuntimeException("bootstrap can not be null");
        }
        //格式不正确
        if(!providerIP.contains(":")){
            return;
        }
        String[] providerAddress = providerIP.split(":");
        String ip = providerAddress[0];
        int port = Integer.parseInt(providerAddress[1]);
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        String providerUrlInfo = URL_MAP.get(providerServiceName).get(providerIP);
        ProviderNodeInfo providerNodeInfo = URL.buildUrlFromUrlStr(providerUrlInfo);
        LOGGER.info("与[providerUrlInfo]建立连接" + providerUrlInfo);

        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);
        channelFutureWrapper.setGroup(providerNodeInfo.getGroup());
        channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
        SERVER_ADDRESS.add(providerIP);

        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.getOrDefault(providerServiceName, new ArrayList<>());
        channelFutureWrappers.add(channelFutureWrapper);
        CONNECT_MAP.put(providerServiceName, channelFutureWrappers);
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        ROUTER.refreshRouterArr(selector);
    }

    public static ChannelFuture createChannelFuture(String ip, Integer port) throws InterruptedException{
        return bootstrap.connect(ip, port).sync();
    }

    public static void disConnect(String providerServiceName, String providerIp){
        SERVER_ADDRESS.remove(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if(CommonUtil.isNotEmptyList(channelFutureWrappers)){
            channelFutureWrappers.removeIf(channelFutureWrapper ->
                    providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort()));
        }
    }

    //默认使用随机策略获取ChannelFuture
    public static ChannelFuture getChannelFuture(RpcInvocation rpcInvocation){
        String providerServiceName = rpcInvocation.getTargetServiceName();
        List<ChannelFutureWrapper> channelFutureWrapperList = CONNECT_MAP.get(providerServiceName);
        if(CommonUtil.isEmptyList(channelFutureWrapperList)){
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }
        CLIENT_FILTER_CHAIN.doFilter(channelFutureWrapperList, rpcInvocation);

        ChannelFutureWrapper[] allChannelFutureWrappers = SERVICE_ROUTER_MAP.get(providerServiceName);
        List<ChannelFutureWrapper> channelFutureWrappers = new ArrayList<>();
        for (ChannelFutureWrapper channelFutureWrapper : allChannelFutureWrappers) {
            if(channelFutureWrapperList.contains(channelFutureWrapper)){
                channelFutureWrappers.add(channelFutureWrapper);
            }
        }
        return ROUTER.select(channelFutureWrappers.toArray(new ChannelFutureWrapper[0])).getChannelFuture();
    }
}


