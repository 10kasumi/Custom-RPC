package com.gda.rpc.core.common.event.listener;

import com.gda.rpc.core.common.event.RpcDestroyEvent;
import com.gda.rpc.core.registry.URL;

import java.util.Iterator;

import static com.gda.rpc.core.common.cache.CommonServerCache.PROVIDER_URL_SET;
import static com.gda.rpc.core.common.cache.CommonServerCache.REGISTRY_SERVICE;

public class ServiceDestroyListener implements RpcListener<RpcDestroyEvent> {
    @Override
    public void callBack(Object o) {
        Iterator<URL> urlIterator = PROVIDER_URL_SET.iterator();
        while(urlIterator.hasNext()){
            URL url = urlIterator.next();
            urlIterator.remove();
            REGISTRY_SERVICE.unRegister(url);
        }
    }
}
