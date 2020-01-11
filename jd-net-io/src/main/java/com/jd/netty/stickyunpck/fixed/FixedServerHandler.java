package com.jd.netty.stickyunpck.fixed;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/5/16.
 * ChannelInboundHandlerAdapter extends ChannelHandlerAdapter 用于对网络事件进行读写操作
 */
public class FixedServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 因为多线程，所以使用原子操作类来进行计数
     */
    private static AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * 收到客户端消息，自动触发
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /**
         * FixedLengthFrameDecoder 自动对发送来的消息进行了解码，后续的 ChannelHandler 接收到的 msg 对象是完整的消息包
         * 第二个 ChannelHandler 是 StringDecoder，它将 ByteBuf 解码成字符串对象
         * 第三个 FixedServerHandler 接收到的 msg 对象就是解码后的字符串对象*/
        String body = (String) msg;

        /**
         * 使用 FixedLengthFrameDecoder 固定长度解码器时，通常对于消息实际长度不足构造器要求的长度时，会使用空格进行填充
         * 所以这里接收之后，使用 trime 去掉消息结尾多余的空格。
         */
        String receiveMsg = body.trim();
        System.out.println((atomicInteger.addAndGet(1)) + "--->" + Thread.currentThread().getName() + ",The server receive  order : " + receiveMsg);

        /**回复消息 ，body 中因为已经包含了结尾填充的空格，所以这里直接返回
         * 每次写的时候，同时刷新*/
        ByteBuf respByteBuf = Unpooled.copiedBuffer(body.getBytes());
        ctx.writeAndFlush(respByteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("-----客户端关闭:" + ctx.channel().remoteAddress());
        /**当发生异常时，关闭 ChannelHandlerContext，释放和它相关联的句柄等资源 */
        ctx.close();
    }
}