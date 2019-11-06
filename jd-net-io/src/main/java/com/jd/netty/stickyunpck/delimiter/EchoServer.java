package com.jd.netty.stickyunpck.delimiter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by Administrator on 2018/11/11 0011.
 * Echo 服务器
 */
public class EchoServer {

    public static void main(String[] args) {
        int port = 9898;
        new EchoServer().bind(port);
    }

    public void bind(int port) {
        /**
         * interface EventLoopGroup extends EventExecutorGroup extends ScheduledExecutorService extends ExecutorService
         * 配置服务端的 NIO 线程池,用于网络事件处理，实质上他们就是 Reactor 线程组
         * bossGroup 用于服务端接受客户端连接，workerGroup 用于进行 SocketChannel 网络读写*/
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            /** ServerBootstrap 是 Netty 用于启动 NIO 服务端的辅助启动类，用于降低开发难度
             * */
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println(Thread.currentThread().getName() + ",服务器初始化通道...");

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
                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            /**服务器启动辅助类配置完成后，调用 bind 方法绑定监听端口，调用 sync 方法同步等待绑定操作完成*/
            ChannelFuture f = b.bind(port).sync();

            System.out.println(Thread.currentThread().getName() + ",服务器开始监听端口，等待客户端连接.........");

            /**下面会进行阻塞，等待服务器连接关闭之后 main 方法退出，程序结束* */
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            /**优雅退出，释放线程池资源*/
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}