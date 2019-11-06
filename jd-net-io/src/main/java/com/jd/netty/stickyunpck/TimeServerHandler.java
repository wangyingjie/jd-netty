package com.jd.netty.stickyunpck;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/5/16.
 * ChannelInboundHandlerAdapter extends ChannelHandlerAdapter 用于对网络事件进行读写操作
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

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
         * 这个 msg 已经是解码成功的消息，所以不再需要像以前一样使用 ByteBuf 进行编码
         * 直接转为 string 字符串即可*/
        String body = (String) msg;


        System.out.println((atomicInteger.addAndGet(1)) + "--->" + Thread.currentThread().getName() + ",The server receive  order : " + body);

        /**回复消息
         * copiedBuffer：创建一个新的缓冲区，内容为里面的参数
         * 通过 ChannelHandlerContext 的 write 方法将消息异步发送给客户端
         * 注意解决 TCP 粘包的策略之一就是：在包尾增加回车换行符进行分割
         * System.getProperty("line.separator");屏蔽了 Windows和Linux的区别
         * windows 系统上回车换行符 "\n",Linux 系统上是 "/n"
         * */
        String respMsg = "I am Server，消息接收 success!" + Thread.currentThread().getName() + System.getProperty("line.separator");
        ByteBuf respByteBuf = Unpooled.copiedBuffer(respMsg.getBytes());
        /**
         * 每次写的时候，同时刷新，防止 TCP 粘包
         */
        ctx.writeAndFlush(respByteBuf);

    }

    /*@Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        *//**flush：将消息发送队列中的消息写入到 SocketChannel 中发送给对方，为了频繁的唤醒 Selector 进行消息发送
         * Netty 的 write 方法并不直接将消息写如 SocketChannel 中，调用 write 只是把待发送的消息放到发送缓存数组中，再通过调用 flush
         * 方法，将发送缓冲区的消息全部写入到 SocketChannel 中
         *//*
        ctx.flush();
    }*/

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        System.out.println("-----客户端关闭:" + ctx.channel().remoteAddress());

        /**当发生异常时，关闭 ChannelHandlerContext，释放和它相关联的句柄等资源 */
        ctx.close();
    }
}