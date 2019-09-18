package com.jd.netty.io;

import io.netty.channel.*;

public class ChannelOutboundHandlerA extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("out A");

        System.out.println("======================>" + msg);
        // super.write(ctx, msg, promise);

        ctx.write(msg, promise);
    }
}
