package com.jd.netty.stickyunpck.fixed;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Created by Administrator on 2018/11/11 0011.
 * Fixed 服务器
 */
public class FixedServer {
    public static void main(String[] args) {
        int port = 9898;
        new FixedServer().bind(port);
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
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println(Thread.currentThread().getName() + ",服务器初始化通道...");
                            /**
                             * FixedLengthFrameDecoder(int frameLength)： frameLength：指定单条消息的长度
                             * 为了测试结果明显，这里设置消息长度为 64 字节，之后在发送消息时，会让部分消息长度小于等于 64，然后部分消息长度大于 64，以观察区别
                             * 实际应用中应该根据时间情况进行设置，比如 1024 字节
                             */
                            ch.pipeline().addLast(new FixedLengthFrameDecoder(64));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new FixedServerHandler());
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