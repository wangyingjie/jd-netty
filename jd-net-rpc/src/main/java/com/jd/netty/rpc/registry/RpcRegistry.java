package com.jd.netty.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RpcRegistry {

    private int port;

    public RpcRegistry(int port) {
        this.port = port;
    }

    public void start() {
        // ServerSocket ServerSocketChannel

        // 基于NIO来实现
        // Selector 主线程 worker 子线程
        ServerBootstrap server = new ServerBootstrap();

        // 主
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            server.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            // pipeline 封装成为一个无锁化的任务队列
                            ChannelPipeline pipeline = ch.pipeline();
                            // 自定义编码器
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            // 自定义解码器
                            pipeline.addLast(new LengthFieldPrepender(4));

                            pipeline.addLast("encoder", new ObjectEncoder());

                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                            // 执行属于自己的逻辑
                            pipeline.addLast(new RegistryHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口阻塞
            ChannelFuture channelFuture = server.bind(port).sync();

            System.out.println("server starting port=" + port);

            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {

        }
    }

    public static void main(String[] args) {
        new RpcRegistry(8080).start();
    }

}
