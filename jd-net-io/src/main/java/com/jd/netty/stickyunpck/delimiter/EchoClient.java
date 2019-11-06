package com.jd.netty.stickyunpck.delimiter;



import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
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
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                /**
                                 * 创建分隔符缓冲对象 ByteBuf，使用自定义的 "$_" 作为消息结束符，自己也可以定义为其它的字符作为结束符
                                 *
                                 * DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf delimiter)
                                 * DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf... delimiters)
                                 * 分隔符解码器重载了好几个构造器方法，其中常用的就是上面这两个
                                 *      maxFrameLength：单条消息的最大长度,当达到该长度后仍然没有查找到分隔符时，则抛出 TooLongFrameException 异常
                                 *      防止由于异常码流缺失分隔符导致内存溢出（亲测 Netty 4.1 版本，服务器并未抛出异常，而是客户端被强制断开连接了）
                                 *      delimiter：分隔符缓冲对象,第二个构造器可见可以指定多个结束符
                                 */
                                System.out.println(Thread.currentThread().getName() + ",客户端初始化管道...");
                                ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
                                ch.pipeline().addLast(new StringDecoder());
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
