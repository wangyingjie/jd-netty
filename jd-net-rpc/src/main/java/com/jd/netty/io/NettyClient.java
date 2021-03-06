package com.jd.netty.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

    public static void main(String[] args) {

        connectServer();
    }

    private static void connectServer() {

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        try {
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // https://blog.csdn.net/zhousenshan/article/details/72859923  有数据就发送，避免发送大块数据包
                    //.option(ChannelOption.TCP_NODELAY, false)
                    .handler(new ChannelInitializer<SocketChannel>() {
                                 @Override
                                 protected void initChannel(SocketChannel ch) throws Exception {
                                     ChannelPipeline pipeline = ch.pipeline();
                                     // pipeline.addLast("encoder", new ObjectEncoder());
                                     // pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                     ch.pipeline().addLast(new ClientInHandler());
                                 }
                             }
                    );

            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();

            // channelFuture.channel().writeAndFlush("Hello world").sync();

            channelFuture.channel().closeFuture().sync();

            //channelFuture.channel().close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            workerGroup.shutdownGracefully();
        }
    }


}
