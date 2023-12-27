package com.gda.rpc.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.gda.rpc.core.common.constants.RpcConstants.DEFAULT_DECODE_CHAR;

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf byteBuf) throws Exception {
        byteBuf.writeShort(msg.getMagicNumber());
        byteBuf.writeInt(msg.getContentLength());
        byteBuf.writeBytes(msg.getContent());
        byteBuf.writeBytes(DEFAULT_DECODE_CHAR.getBytes());
    }
}
