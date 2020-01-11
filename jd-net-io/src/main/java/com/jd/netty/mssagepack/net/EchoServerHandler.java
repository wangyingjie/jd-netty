package com.jd.netty.mssagepack.net;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;
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
        System.out.println((atomicInteger.addAndGet(1)) + "--->" + Thread.currentThread().getName() + ",The server receive  order : " + msg);

        /**
         * 如果传输的是 POJO 对象，则可以转成 List<Object>
         * list 中的每一个元素都是发送来的 POJO 对象的属性值
         * 注意：如果对方传输只是简单的 String 对象，则不能强转为 List<Object>
         */
        List<Object> objects = (List<Object>) msg;
        for (Object obj : objects) {
            System.out.println("属性：" + obj);
        }

        /**
         * 服务端接收到客户端发送来的数据后，再回发给客户端
         */
        ctx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("-----客户端关闭:" + ctx.channel().remoteAddress());
        /**当发生异常时，关闭 ChannelHandlerContext，释放和它相关联的句柄等资源 */
        ctx.close();
    }
}