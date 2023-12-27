package com.gda.rpc.core.common.excrption;

import com.gda.rpc.core.common.RpcInvocation;

public class RpcException extends RuntimeException{

    private RpcInvocation rpcInvocation;

    public RpcException(RpcInvocation rpcInvocation){
        this.rpcInvocation = rpcInvocation;
    }

    public RpcInvocation getRpcInvocation(){
        return rpcInvocation;
    }

    public void setRpcInvocation(RpcInvocation rpcInvocation) {
        this.rpcInvocation = rpcInvocation;
    }
}
