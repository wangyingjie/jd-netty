package com.jd.netty.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChannelInboundHandlerA extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("in A ");

        // super.channelRead(ctx, msg);

        String str = (String) msg;

        // ctx.writeAndFlush(str + "  77777 ");

        System.out.println("str============> " + str);

        ctx.fireChannelRead(str + "  77777 ");
    }
}
