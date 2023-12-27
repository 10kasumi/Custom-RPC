package com.gda.rpc.core.router;


import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.registry.URL;

public interface Router {
    /**
     * 刷新路由数组
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到请求的连接通道
     */
    ChannelFutureWrapper select(ChannelFutureWrapper[] channelFutureWrappers);

    //更新权重
    void updateWight(URL url);
}
