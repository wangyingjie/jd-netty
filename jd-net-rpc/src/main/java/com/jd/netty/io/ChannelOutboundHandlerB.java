package com.jd.netty.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.TimeUnit;

public class ChannelOutboundHandlerB extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("out B");

        System.out.println("======================>" + msg);
        // super.write(ctx, msg, promise);

        ctx.write(msg, promise);
    }

    /*
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        ctx.executor().schedule(new Runnable() {
            @Override
            public void run() {
                ctx.channel().write("say hello");
            }
        },3, TimeUnit.SECONDS);
    }
    */

}
