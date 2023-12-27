package com.gda.rpc.core.filter.server;

import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.utils.CommonUtil;
import com.gda.rpc.core.filter.ServerFilter;
import com.gda.rpc.core.server.ServiceWrapper;

import static com.gda.rpc.core.common.cache.CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP;

public class ServerTokenFilterImpl implements ServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String token = String.valueOf(rpcInvocation.getAttachments().get("serviceToken"));
        if(!PROVIDER_SERVICE_WRAPPER_MAP.containsKey(rpcInvocation.getTargetServiceName())){
            return;
        }
        ServiceWrapper serviceWrapper = PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = String.valueOf(serviceWrapper.getServiceToken());
        if(CommonUtil.isEmpty(matchToken)) return;
        if(CommonUtil.isNotEmpty(token) && token.equals(matchToken)) return;
        throw new RuntimeException("token is " + token + ", verify result is false");
    }
}
