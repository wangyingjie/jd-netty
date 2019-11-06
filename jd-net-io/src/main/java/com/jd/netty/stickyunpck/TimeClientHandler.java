package com.jd.netty.stickyunpck;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/5/17.
 * 用于对网络事件进行读写操作
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());


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
         * 连续发送 50 条数据
         */
        for (int i = 0; i < 10; i++) {
            /**
             * 解决 TCP 粘包的策略之一就是：在包尾增加回车换行符进行分割
             * System.getProperty("line.separator");屏蔽了 Windows和Linux的区别
             * windows 系统上回车换行符 "\n",Linux 上是 "/n"
             */
            String reqMsg = (i + 1) + ",我是客户端 " + Thread.currentThread().getName() + System.getProperty("line.separator");
            byte[] reqMsgByte = reqMsg.getBytes("UTF-8");
            ByteBuf reqByteBuf = Unpooled.buffer(reqMsgByte.length);
            /**
             * writeBytes：将指定的源数组的数据传输到缓冲区
             * 调用 ChannelHandlerContext 的 writeAndFlush 方法将消息发送给服务器
             */
            reqByteBuf.writeBytes(reqMsgByte);
            /**
             * 每次发送的同时进行刷新
             */
            ctx.writeAndFlush(reqByteBuf);
        }
    }

    /**
     * 当服务端返回应答消息时，channelRead 方法被调用，从 Netty 的 ByteBuf 中读取并打印应答消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /**
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