package com.gda.rpc.core.client;

import com.alibaba.fastjson.JSON;
import com.gda.rpc.core.common.RpcDecoder;
import com.gda.rpc.core.common.RpcEncoder;
import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.RpcProtocol;
import com.gda.rpc.core.common.config.PropertiesBootstrap;
import com.gda.rpc.core.common.event.RpcListenerLoader;
import com.gda.rpc.core.common.utils.CommonUtil;
import com.gda.rpc.core.filter.ClientFilter;
import com.gda.rpc.core.filter.client.ClientFilterChain;
import com.gda.rpc.core.proxy.ProxyFactory;
import com.gda.rpc.core.registry.AbstractRegister;
import com.gda.rpc.core.registry.RegistryService;
import com.gda.rpc.core.registry.URL;
import com.gda.rpc.core.router.Router;
import com.gda.rpc.core.serialize.SerializeFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.gda.rpc.core.common.cache.CommonClientCache.*;
import static com.gda.rpc.core.common.constants.RpcConstants.DEFAULT_DECODE_CHAR;
import static com.gda.rpc.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

public class Client {

    private AbstractRegister abstractRegister;

    private final Bootstrap bootstrap = new Bootstrap();

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public RpcReference initClientApplication() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        NioEventLoopGroup clientNioEventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientNioEventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ByteBuf byteBuf = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(CLIENT_CONFIG.getMaxServerRespDataSize(), byteBuf));
                socketChannel.pipeline().addLast(new RpcEncoder());
                socketChannel.pipeline().addLast(new RpcDecoder());
                socketChannel.pipeline().addLast(new ClientHandler());
            }
        });

        ConnectionHandler.setBootstrap(bootstrap);

        RpcListenerLoader rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();

        String routerStrategy = CLIENT_CONFIG.getRouterStrategy();
        EXTENSION_LOADER.loadExtension(Router.class);
        LinkedHashMap<String, Class<?>> routerMap = EXTENSION_LOADER_CLASS_CACHE.get(Router.class.getName());
        Class<?> routerClass = routerMap.get(routerStrategy);
        if(routerClass == null){
            throw new RuntimeException("no match routerStrategyClass for " + routerStrategy);
        }
        ROUTER = (Router) routerClass.newInstance();

        String clientSerialize = CLIENT_CONFIG.getClientSerialize();
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class<?>> serializeMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class<?> serializeClass = serializeMap.get(clientSerialize);
        if(serializeClass == null){
            throw new RuntimeException("no match serialFactory for " + clientSerialize);
        }
        CLIENT_SERIALIZE_FACTORY = (SerializeFactory) serializeClass.newInstance();

        ClientFilterChain clientFilterChain = new ClientFilterChain();
        EXTENSION_LOADER.loadExtension(ClientFilter.class);
        LinkedHashMap<String, Class<?>> filterChainMap = EXTENSION_LOADER_CLASS_CACHE.get(ClientFilter.class.getName());
        for (Map.Entry<String, Class<?>> filterChainEntry : filterChainMap.entrySet()) {
            String filterChainKey = filterChainEntry.getKey();
            Class<?> filterChainImpl = filterChainEntry.getValue();
            if(filterChainImpl == null){
                throw new RuntimeException("no match filterChainImpl for " + filterChainKey);
            }
            clientFilterChain.addClientFilter((ClientFilter) filterChainImpl.newInstance());
        }
        CLIENT_FILTER_CHAIN = clientFilterChain;

        //初始化代理工厂
        String proxyType = CLIENT_CONFIG.getProxyType();
        EXTENSION_LOADER.loadExtension(ProxyFactory.class);
        LinkedHashMap<String, Class<?>> proxyTypeMap = EXTENSION_LOADER_CLASS_CACHE.get(ProxyFactory.class.getName());
        Class<?> proxyTypeClass = proxyTypeMap.get(proxyType);
        if(proxyTypeClass == null){
            throw new RuntimeException("no match proxyTypeClass for " + proxyType);
        }
        return new RpcReference((ProxyFactory) proxyTypeClass.newInstance());
    }

    public void initClientConfig(){
        CLIENT_CONFIG = PropertiesBootstrap.loadClientConfigFromLocal();
    }

    /**
     * 启动服务前需要订阅对应的 provider服务
     * @param serviceBean
     */
    public void doSubscribeService(Class<?> serviceBean){
        if(abstractRegister == null){
            try{
                String registerType = CLIENT_CONFIG.getRegisterType();
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                LinkedHashMap<String, Class<?>> registerMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class<?> registerClass = registerMap.get(registerType);
                abstractRegister = (AbstractRegister) registerClass.newInstance();
            } catch (Exception e){
                throw new RuntimeException("registryServiceType unKnow, error is ", e);
            }
        }
        URL url = new URL();
        url.setApplicationName(CLIENT_CONFIG.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtil.getIpAddress());
        Map<String, String> result = abstractRegister.getServiceWeightMap(serviceBean.getName());
        URL_MAP.put(serviceBean.getName(), result);
        abstractRegister.subscribe(url);
    }

    /**
     * 和provider连接
     */
    public void doConnectServer(){
        for (URL providerUrl : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerUrl.getServiceName());
            for(String providerIp : providerIps){
                try{
                    ConnectionHandler.connect(providerUrl.getServiceName(), providerIp);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            URL url = new URL();
            url.setServiceName(providerUrl.getServiceName());
            url.addParameter("providerIps", JSON.toJSONString(providerIps));

            abstractRegister.doAfterSubscribe(url);
        }
    }

    public void startClient(){
        Thread asyncSendJob = new Thread(new AsyncSendJob(), "ClientAsyncSendJobThread");
        asyncSendJob.start();
    }

    class AsyncSendJob implements Runnable{
        public AsyncSendJob(){

        }

        @Override
        public void run() {
            while(true){
                try{
                    RpcInvocation rpcInvocation = SEND_QUEUE.take();
                    byte[] data = CLIENT_SERIALIZE_FACTORY.serialize(rpcInvocation);
                    RpcProtocol rpcProtocol = new RpcProtocol(data);
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(rpcInvocation);
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}

