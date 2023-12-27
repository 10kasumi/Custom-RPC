package com.gda.rpc.core.common.cache;

import com.gda.rpc.core.common.ChannelFuturePollingRef;
import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.config.ClientConfig;
import com.gda.rpc.core.filter.client.ClientFilterChain;
import com.gda.rpc.core.registry.URL;
import com.gda.rpc.core.router.Router;
import com.gda.rpc.core.serialize.SerializeFactory;
import com.gda.rpc.core.spi.ExtensionLoader;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公用缓存，存储请求队列等公共信息
 */
public class CommonClientCache {

    //发送阻塞队列
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(100);

    //保存处理结果 <UUID, Object>
    public static Map<String, Object> RESP_MAP = new ConcurrentHashMap<>();

    //当前client订阅了哪些服务 serviceName->URL
    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();

    //com.xxx.service -> <<ip:host, urlString>, <ip:host, urlString>>
    public static Map<String, Map<String, String>> URL_MAP = new ConcurrentHashMap<>();

    //记录所有服务提供者的ip和端口
    public static Set<String> SERVER_ADDRESS = new HashSet<>();

    //保存服务端的路由
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();

    //每次远程调用的时候都从这里面选择服务提供者
    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();

    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();


    public static Router ROUTER;

    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;

    public static ClientFilterChain CLIENT_FILTER_CHAIN;

    public static ClientConfig CLIENT_CONFIG;

    //spi加载组件
    public static ExtensionLoader EXTENSION_LOADER = new ExtensionLoader();
}
