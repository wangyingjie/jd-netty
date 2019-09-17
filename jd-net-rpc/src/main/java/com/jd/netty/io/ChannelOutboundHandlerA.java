package com.jd.netty.io;

import io.netty.channel.*;

public class ChannelOutboundHandlerA extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("out A");

        // super.write(ctx, msg, promise);

        ctx.fireChannelRead(msg);
    }
}
