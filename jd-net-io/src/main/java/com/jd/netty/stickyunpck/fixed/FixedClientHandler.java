package com.jd.netty.stickyunpck.fixed;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/5/17.
 * 用于对网络事件进行读写操作
 */
public class FixedClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 因为 Netty 采用线程池，所以这里使用原子操作类来进行计数
     */
    private static AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * 当客户端和服务端 TCP 链路建立成功之后，Netty 的 NIO 线程会调用 channelActive 方法
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        /**
         * 连续发送 10 条数据
         */
        for (int i = 0; i < 10; i++) {
            /** DelimiterBasedFrameDecoder 解码器自定义的分隔符是 "$_"，所以发送消息时要在结尾处加上*/
            String reqMsg = (i + 1) + "-我是客户端 " + Thread.currentThread().getName() + "";
            //byte[] reqMsgByte = reqMsg.getBytes("UTF-8");

            byte[] bytes1 = reqMsg.getBytes("UTF-8");

            byte[] msgBytes1 = new byte[64];
            for (int j = 0; j < msgBytes1.length; j++) {
                if (j < bytes1.length) {
                    msgBytes1[j] = bytes1[j];
                } else {
                    /**32 表示空格，等价于：msgBytes1[i] = " ".getBytes()[0];*/
                    msgBytes1[j] = 32;
                }
            }

            // System.out.println(new String(msgBytes1, "UTF-8") + "," + msgBytes1.length);

            ByteBuf reqByteBuf = Unpooled.buffer(msgBytes1.length);
            if (i == 9) {

                /** writeBytes：将指定的源数组的数据传输到缓冲区
                 * 调用 ChannelHandlerContext 的 writeAndFlush 方法将消息发送给服务器*/
                reqByteBuf.writeBytes("0123456789012345678901234567890123456789012345678901234567891234".getBytes());
            } else if (i == 5) {

                reqByteBuf.writeBytes("0123456789012345678901234567890123456789012345678901234567891234abcdef".getBytes());
            } else {
                reqByteBuf.writeBytes(msgBytes1);

            }


            /** 每次发送的同时进行刷新*/
            ctx.writeAndFlush(reqByteBuf);
        }
    }

    /**
     * 当服务端返回应答消息时，channelRead 方法被调用，从 Netty 的 ByteBuf 中读取并打印应答消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /**与服务器同理
         * 这个 msg 已经是解码成功的消息，所以不再需要像以前一样使用 ByteBuf 进行编码
         * 直接转为 string 字符串即可*/
        String body = (String) msg;
        System.out.println((atomicInteger.addAndGet(1)) + "---" + Thread.currentThread().getName() + ",Server return Message：" + body);
    }

    /**
     * 当发生异常时，打印异常 日志，释放客户端资源
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /**释放资源*/
        ctx.close();
    }
}