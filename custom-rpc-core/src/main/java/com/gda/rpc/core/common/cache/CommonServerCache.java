package com.gda.rpc.core.common.cache;

import com.gda.rpc.core.common.ServerServiceSemaphoreWrapper;
import com.gda.rpc.core.common.config.ServerConfig;
import com.gda.rpc.core.dispatcher.ServerChannelDispatcher;
import com.gda.rpc.core.filter.server.ServerAfterFilterChain;
import com.gda.rpc.core.filter.server.ServerBeforeFilterChain;
import com.gda.rpc.core.registry.RegistryService;
import com.gda.rpc.core.registry.URL;
import com.gda.rpc.core.serialize.SerializeFactory;
import com.gda.rpc.core.server.ServiceWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommonServerCache {

    /**
     * 存放需要注册的对象
     */
    public static final Map<String, Object> PROVIDER_CLASS_MAP = new HashMap<>();

    /**
     * 服务提供者的URL
     */
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();

    /**
     * 注册中心：负责服务端服务的注册和下线
     */
    public static RegistryService REGISTRY_SERVICE;

    public static SerializeFactory SERVER_SERIALIZE_FACTORY;

    public static ServerBeforeFilterChain SERVER_BEFORE_FILTER_CHAIN;

    public static ServerAfterFilterChain SERVER_AFTER_FILTER_CHAIN;

    public static ServerConfig SERVER_CONFIG;

    /**
     * 过滤链的map: <serviceName, 服务端包装类>
     */
    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();

    public static ServerChannelDispatcher SERVER_CHANNEL_DISPATCHER = new ServerChannelDispatcher();

    /**
     * 服务端限流
     */
    public static final Map<String, ServerServiceSemaphoreWrapper> SERVER_SERVICE_SEMAPHORE_MAP = new ConcurrentHashMap<>();
}

