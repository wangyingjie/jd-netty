package com.jd.netty.stickyunpck.fixed;



import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Created by Administrator on 2017/5/16.
 * Fixed 客户端
 */
public class FixedClient {

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
                                System.out.println(Thread.currentThread().getName() + ",客户端初始化管道...");
                                /**
                                 * 与服务器同理
                                 * FixedLengthFrameDecoder(int frameLength)： frameLength：指定单条消息的长度
                                 * 为了测试结果明显，这里设置消息长度为 64 字节，实际应用中应该根据时间情况进行设置，比如 1024 字节
                                 */
                                ch.pipeline().addLast(new FixedLengthFrameDecoder(64));
                                ch.pipeline().addLast(new StringDecoder());
                                ch.pipeline().addLast(new FixedClientHandler());
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
