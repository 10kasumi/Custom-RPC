package com.gda.rpc.core.server;

import com.gda.rpc.core.common.RpcDecoder;
import com.gda.rpc.core.common.RpcEncoder;
import com.gda.rpc.core.common.ServerServiceSemaphoreWrapper;
import com.gda.rpc.core.common.annotations.SPI;
import com.gda.rpc.core.common.config.PropertiesBootstrap;
import com.gda.rpc.core.common.event.RpcListenerLoader;
import com.gda.rpc.core.common.utils.CommonUtil;
import com.gda.rpc.core.filter.ServerFilter;
import com.gda.rpc.core.filter.server.ServerAfterFilterChain;
import com.gda.rpc.core.filter.server.ServerBeforeFilterChain;
import com.gda.rpc.core.registry.AbstractRegister;
import com.gda.rpc.core.registry.RegistryService;
import com.gda.rpc.core.registry.URL;
import com.gda.rpc.core.serialize.SerializeFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.gda.rpc.core.common.cache.CommonClientCache.EXTENSION_LOADER;
import static com.gda.rpc.core.common.cache.CommonServerCache.*;
import static com.gda.rpc.core.common.constants.RpcConstants.DEFAULT_DECODE_CHAR;
import static com.gda.rpc.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

public class Server {

    public void startServerApplication() throws InterruptedException, IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);

        serverBootstrap.handler(new MaxConnectionLimitHandler(SERVER_CONFIG.getMaxConnections()));
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(SERVER_CONFIG.getMaxServerRequestData(), delimiter));
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ServerHandler());
            }
        });

        RpcListenerLoader rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();

        String serverSerialize = SERVER_CONFIG.getServerSerialize();
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class<?>> serializeMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class<?> serializeClass = serializeMap.get(serverSerialize);
        if (serializeClass == null) {
            throw new RuntimeException("no match serializeClass for " + serverSerialize);
        }
        SERVER_SERIALIZE_FACTORY = (SerializeFactory) serializeClass.newInstance();

        ServerBeforeFilterChain serverBeforeFilterChain = new ServerBeforeFilterChain();
        ServerAfterFilterChain serverAfterFilterChain = new ServerAfterFilterChain();
        EXTENSION_LOADER.loadExtension(ServerFilter.class);
        LinkedHashMap<String, Class<?>> filterChainMap = EXTENSION_LOADER_CLASS_CACHE.get(ServerFilter.class.getName());
        for (Map.Entry<String, Class<?>> filterChainEntry : filterChainMap.entrySet()) {
            String filterChainKey = filterChainEntry.getKey();
            Class<?> filterChainImpl = filterChainEntry.getValue();
            if(filterChainImpl == null){
                throw new RuntimeException("no match filterChainImpl for " + filterChainKey);
            }
            SPI spi = (SPI) filterChainImpl.getDeclaredAnnotation(SPI.class);
            if(spi != null && "before".equalsIgnoreCase(spi.value())){
                serverBeforeFilterChain.addServerFilter(((ServerFilter) filterChainImpl.newInstance()));
            } else if(spi != null && "after".equalsIgnoreCase(spi.value())){
                serverAfterFilterChain.addServerFilter((ServerFilter) filterChainImpl.newInstance());
            }
        }
        SERVER_BEFORE_FILTER_CHAIN = serverBeforeFilterChain;
        SERVER_AFTER_FILTER_CHAIN = serverAfterFilterChain;

        //初始化dispatcher
        SERVER_CHANNEL_DISPATCHER.init(SERVER_CONFIG.getServerQueueSize(), SERVER_CONFIG.getServerBizThreadNums());
        SERVER_CHANNEL_DISPATCHER.startDataConsume();

        //暴露服务端url
        this.batchExportUrl();
        serverBootstrap.bind(SERVER_CONFIG.getPort()).sync();
    }

    public void initServerConfig(){
        SERVER_CONFIG = PropertiesBootstrap.loadServerConfigFromLocal();
    }

    public void batchExportUrl(){
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for(URL url : PROVIDER_URL_SET){
                    REGISTRY_SERVICE.register(url);
                }
            }
        });
        task.start();
    }

    public void registryService(ServiceWrapper serviceWrapper){
        Object serviceBean = serviceWrapper.getServiceBean();
        if(serviceBean.getClass().getInterfaces().length == 0){
            throw new RuntimeException("service must had interfaces!");
        }
        Class<?>[] classes = serviceBean.getClass().getInterfaces();
        if(classes.length > 1){
            throw new RuntimeException("service must only had one interfaces!");
        }
        if(REGISTRY_SERVICE == null){
            try{
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                Map<String, Class<?>> registryClassMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class<?> registryClass = registryClassMap.get(SERVER_CONFIG.getRegisterType());
                REGISTRY_SERVICE = (AbstractRegister) registryClass.newInstance();
            } catch (Exception e){
                throw new RuntimeException("registryServiceType unKnow, error is ", e);
            }
        }
        Class<?> interfaceClass = classes[0];
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
        URL url = new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(SERVER_CONFIG.getApplicationName());
        url.addParameter("host", CommonUtil.getIpAddress());
        url.addParameter("port", String.valueOf(SERVER_CONFIG.getPort()));
        url.addParameter("group", String.valueOf(serviceWrapper.getGroup()));
        url.addParameter("limit", String.valueOf(serviceWrapper.getLimit()));
        url.addParameter("weight", String.valueOf(serviceWrapper.getWeight()));
        PROVIDER_URL_SET.add(url);
        if(serviceWrapper.getLimit() > 0){
            SERVER_SERVICE_SEMAPHORE_MAP.put(interfaceClass.getName(), new ServerServiceSemaphoreWrapper(serviceWrapper.getLimit()));
        }
        if(CommonUtil.isNotEmpty(serviceWrapper.getServiceToken())){
            PROVIDER_SERVICE_WRAPPER_MAP.put(interfaceClass.getName(), serviceWrapper);
        }
    }
}
