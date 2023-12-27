package com.gda.rpc.core.server;

import com.gda.rpc.core.common.event.RpcDestroyEvent;
import com.gda.rpc.core.common.event.RpcListenerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerShutdownHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerShutdownHook.class);

    /**
     *
     */
    public static void registryShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                RpcListenerLoader.sendSyncEvent(new RpcDestroyEvent("destroy"));
                LOGGER.info("server destruction");
            }
        }));
    }
}
