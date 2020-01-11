package com.jd.netty.ch02;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettyClient implements Runnable {

    @Override
    public void run() {

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // ch.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4))
                            ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4));
                            ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast("handler", new ClientHandler());

                        }
                    });

            for (int i = 0; i < 10; i++) {
                // 让创建连接成为同步的
                ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
                // 客户端写数据并刷新
                channelFuture.channel().writeAndFlush("Hello server this thread is " + Thread.currentThread().getName() + "  ==== " + i);
                //
                channelFuture.channel().closeFuture().sync();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(new NettyClient()).start();
        }
    }
}
