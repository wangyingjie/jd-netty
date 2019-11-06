package com.jd.netty.stickyunpck.delimiter;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/5/16.
 * ChannelInboundHandlerAdapter extends ChannelHandlerAdapter 用于对网络事件进行读写操作
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

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
         * DelimiterBasedFrameDecoder 自动对发送来的消息进行了解码，后续的 ChannelHandler 接收到的 msg 对象是完整的消息包
         * 第二个 ChannelHandler 是 StringDecoder，它将 ByteBuf 解码成字符串对象
         * 第三个 EchoServerHandler 接收到的 msg 对象就是解码后的字符串对象*/
        String body = (String) msg;
        System.out.println((atomicInteger.addAndGet(1)) + "--->" + Thread.currentThread().getName() + ",The server receive  order : " + body);

        /**回复消息
         * 由于创建的 DelimiterBasedFrameDecoder 解码器默认会自动去掉分隔符，所以返回给客户端时需要自己拼接分隔符
         * DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter)
         *  这个构造器可以设置是否去除分隔符
         * 最后创建 ByteBuf 将原始的消息重新返回给客户端
         * */
        String respMsg = body + "$_";
        ByteBuf respByteBuf = Unpooled.copiedBuffer(respMsg.getBytes());
        /**
         * 每次写的时候，同时刷新，防止 TCP 粘包
         */
        ctx.writeAndFlush(respByteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("-----客户端关闭:" + ctx.channel().remoteAddress());
        /**当发生异常时，关闭 ChannelHandlerContext，释放和它相关联的句柄等资源 */
        ctx.close();
    }
}
