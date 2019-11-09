package com.jd.netty.mssagepack.net;


import com.jd.netty.mssagepack.pck.MsgpackDecoder;
import com.jd.netty.mssagepack.pck.MsgpackEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 1）虽然 MessagePack 用于序列化对象，但是普通 String、Integer 等等同样也是对象，所以照样可以传输普通的字符串等消息
 * 2）需要序列化的 POJO 对象上必须加上 org.msgpack.annotation.Message 注解：@Message，否则传输会失败，而且也不报错，很难排查
 * 3）MessagePack 序列化对象后的消息，经过发送后，接收端 channelRead(ChannelHandlerContext ctx, Object msg)
 *     3.1）即使发送的是 User 对象，接收端的 msg 也不能进行 User user = (User)msg 强转，否则客户端会被强制断开连接
 *     3.2）如果发送的是 User 对象，接收端可以转为 List<Object> objects = (List<Object>) msg，list 中的元素对应 User 的属性值
 *     3.3）如果发送的不是 POJO 对象，而是简单的 String 对象，则不能转为 List<Object>，否则客户端也会被强制断开
 *
 * https://blog.csdn.net/wangmx1993328/article/details/84486516
 *
 * Created by Administrator on 2017/5/16.
 * Echo 客户端
 */
public class EchoClient {

    /**
     * 使用 2 个线程模拟 2 个客户端
     *
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 0; i < 2; i++) {
            new Thread(new MyThread()).start();
        }
    }

    static class MyThread implements Runnable {

        @Override
        public void run() {
            connect("127.0.0.1", 9898);
        }

        public void connect(String host, int port) {
            /**配置客户端 NIO 线程组/池*/
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                /**Bootstrap 与 ServerBootstrap 都继承(extends)于 AbstractBootstrap
                 * 创建客户端辅助启动类,并对其配置,与服务器稍微不同，这里的 Channel 设置为 NioSocketChannel
                 * 然后为其添加 Handler，这里直接使用匿名内部类，实现 initChannel 方法
                 * 作用是当创建 NioSocketChannel 成功后，在进行初始化时,将它的ChannelHandler设置到ChannelPipeline中，用于处理网络I/O事件*/
                Bootstrap b = new Bootstrap();
                b.group(group).channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        // 设置TCP连接超时时间
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                System.out.println(Thread.currentThread().getName() + ",客户端初始化管道...");
                                /**
                                 * 为了处理半包消息，添加如下两个 Netty 内置的编解码器
                                 * LengthFieldPrepender：前置长度域编码器——放在MsgpackEncoder编码器前面
                                 * LengthFieldBasedFrameDecoder：长度域解码器——放在MsgpackDecoder解码器前面
                                 * 关于 长度域编解码器处理半包消息，本文不做详细讲解，会有专门篇章进行说明
                                 */
                                ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
                                ch.pipeline().addLast("MessagePack encoder", new MsgpackEncoder());
                                ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                                ch.pipeline().addLast("MessagePack Decoder", new MsgpackDecoder());
                                ch.pipeline().addLast(new EchoClientHandler());
                            }
                        });

                /**connect：发起异步连接操作，调用同步方法 sync 等待连接成功*/
                ChannelFuture channelFuture = b.connect(host, port).sync();
                System.out.println(Thread.currentThread().getName() + ",客户端发起异步连接..........");

                /**等待客户端链路关闭*/
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                /**优雅退出，释放NIO线程组*/
                group.shutdownGracefully();
            }
        }
    }
}