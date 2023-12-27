package com.gda.rpc.core.filter.client;

import com.gda.rpc.core.common.ChannelFutureWrapper;
import com.gda.rpc.core.common.RpcInvocation;
import com.gda.rpc.core.common.utils.CommonUtil;
import com.gda.rpc.core.filter.ClientFilter;

import java.util.List;

/**
 * 服务分组过滤器
 */
public class GroupFilterImpl implements ClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String group = String.valueOf(rpcInvocation.getAttachments().get("group"));
        src.removeIf(channelFutureWrapper -> !channelFutureWrapper.getGroup().equals(group));
        if(CommonUtil.isEmptyList(src)){
            throw new RuntimeException("no provider match for group " + group);
        }
    }
}
