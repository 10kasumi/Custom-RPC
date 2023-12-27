package com.gda.rpc.core.filter.server;

import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.annotations.SPI;
import com.gda.rpc.core.filter.ServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPI("before")
public class ServerLogFilterImpl implements ServerFilter {

    private final Logger logger = LoggerFactory.getLogger(ServerLogFilterImpl.class);

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        logger.info(rpcInvocation.getAttachments().get("c_app_name") + " do invoke -> " +
            rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
