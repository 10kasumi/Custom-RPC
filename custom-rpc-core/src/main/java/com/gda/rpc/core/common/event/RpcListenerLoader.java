package com.gda.rpc.core.common.event;

import com.gda.rpc.core.common.event.listener.RpcListener;
import com.gda.rpc.core.common.utils.CommonUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gda.rpc.core.common.cache.CommonClientCache.EXTENSION_LOADER;
import static com.gda.rpc.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

public class RpcListenerLoader {

    private static List<RpcListener<?>> rpcListenerList = new ArrayList<>();

    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    public static void registerListener(RpcListener<?> rpcListener) {
        rpcListenerList.add(rpcListener);
    }

    public void init(){
        try{
            EXTENSION_LOADER.loadExtension(RpcListener.class);
            LinkedHashMap<String, Class<?>> listenerMap = EXTENSION_LOADER_CLASS_CACHE.get(RpcListener.class.getName());
            for(Map.Entry<String, Class<?>> listenerEntry : listenerMap.entrySet()){
                String key = listenerEntry.getKey();
                Class<?> listener = listenerEntry.getValue();
                registerListener((RpcListener<?>) listener.newInstance());
            }
        } catch (Exception e){
            throw new RuntimeException("registerListener unKnow, error is ", e);
        }
    }

    public static void sendEvent(RpcEvent rpcEvent){
        if(CommonUtil.isEmptyList(rpcListenerList)){
            return;
        }
        for (RpcListener<?> rpcListener : rpcListenerList) {
            Class<?> type = getInterfaceType(rpcListener);
            if(type != null && type.equals(rpcEvent.getClass())){
                eventThreadPool.execute(() -> {
                    try{
                        rpcListener.callBack(rpcEvent.getData());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    public static void sendSyncEvent(RpcEvent rpcEvent){
        if(CommonUtil.isEmptyList(rpcListenerList)){
            return;
        }
        for (RpcListener<?> rpcListener : rpcListenerList) {
            Class<?> type = getInterfaceType(rpcListener);
            if(type != null && type.equals(rpcEvent.getClass())){
                try{
                    rpcListener.callBack(rpcEvent.getData());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static Class<?> getInterfaceType(Object o){
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if(type instanceof Class<?>){
            return (Class<?>) type;
        }
        return null;
    }
}


