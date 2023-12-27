package com.gda.rpc.core.common.excrption;

import com.gda.rpc.core.common.RpcInvocation;

public class MaxServiceLimitRequestException extends RpcException{
    public MaxServiceLimitRequestException(RpcInvocation rpcInvocation) {
        super(rpcInvocation);
    }
}
