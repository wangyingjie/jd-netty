package com.jd.netty.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChannelInboundHandlerB extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("in B ");

        // super.channelRead(ctx, msg);

        String str = (String) msg;
        System.out.println("str=====2=======> " + str);

        // ctx.writeAndFlush(str + "  zhangsan ");

        ctx.fireChannelRead(str + "  8888 ");
    }


}
