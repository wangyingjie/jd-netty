package com.jd.netty.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyClient2 {

    public static void main(String[] args) {

        connectServer();
    }

    private static void connectServer() {
        NioEventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // https://blog.csdn.net/zhousenshan/article/details/72859923  有数据就发送，避免发送大块数据包
                    .option(ChannelOption.TCP_NODELAY, false)
                    .handler(new ChannelInitializer<SocketChannel>() {
                                 @Override
                                 protected void initChannel(SocketChannel ch) throws Exception {
                                     ChannelPipeline pipeline = ch.pipeline();

                                     pipeline.addLast("encoder", new StringEncoder());
                                     pipeline.addLast("decoder", new StringDecoder());
                                 }
                             }

                    );

            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();

            channelFuture.channel().writeAndFlush("Hello world").sync();

            channelFuture.channel().close();
            // channelFuture.channel().close()


        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            // 关闭线程池
           // group.shutdownGracefully();
        }
    }


}
