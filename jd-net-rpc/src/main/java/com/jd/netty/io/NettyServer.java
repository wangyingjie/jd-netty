package com.jd.netty.io;

import com.jd.netty.pipeline.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {


    public static void main(String[] args) {

        start();
    }


    private static void start() {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // netty 大动脉
                            ChannelPipeline pipeline = ch.pipeline();

                            // pipeline.addLast("encoder", new ObjectEncoder());
                            // pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                            ch.pipeline().addLast(new ChannelInboundHandlerA());
                            ch.pipeline().addLast(new ChannelInboundHandlerB());

                            ch.pipeline().addLast(new ChannelOutboundHandlerA());
                            ch.pipeline().addLast(new ChannelOutboundHandlerB());
                            ch.pipeline().addLast(new ChannelOutboundHandlerC());

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture sync = bootstrap.bind(8080).sync();

            System.out.println("server starting port=" + 8080);

            sync.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
