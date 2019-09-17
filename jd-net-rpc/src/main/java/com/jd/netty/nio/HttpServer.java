package com.jd.netty.nio;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;

public class HttpServer {


    private static final int port = 8090;

    // public HttpServer(int port) {
    //     this.port = port;
    // }

    public static void main(String[] args) throws Exception {
        // if (args.length != 1) {
        //     System.err.println(
        //             "Usage: " + HttpServer.class.getSimpleName() +
        //                     " <port>");
        //     return;
        // }
        // int port = Integer.parseInt(args[0]);
        new HttpServer().start(port);
    }

    public void start(int port) throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                            throws Exception {
                        System.out.println("initChannel ch:" + ch);
                        ch.pipeline()
                                .addLast("decoder", new HttpRequestDecoder())   // 1
                                .addLast("encoder", new HttpResponseEncoder())  // 2
                                .addLast("aggregator", new HttpObjectAggregator(512 * 1024))    // 3
                                .addLast("handler", new HttpHandler());        // 4
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128) // determining the number of connections queued
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);

        ChannelFuture channelFuture = b.bind(port).sync();

        //channelFuture.channel().closeFuture().sync();
    }

    private static class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> { // 1

        private AsciiString contentType = HttpHeaderValues.TEXT_PLAIN;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
            System.out.println("class:" + msg.getClass().getName());
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer("test1234234".getBytes())); // 2

            // 生成response，这里使用的FullHttpResponse，同FullHttpRequest类似，通过这个我们就不用将response拆分成多个channel返回给请求端了。
            // 添加header描述length。这一步是很重要的一步，如果没有这一步，你会发现用postman发出请求之后就一直在刷新，因为http请求方不知道返回的数据到底有多长。
            // channel读取完成之后需要输出缓冲流。如果没有这一步，你会发现postman同样会一直在刷新。

            HttpHeaders heads = response.headers();
            heads.add(HttpHeaderNames.CONTENT_TYPE, contentType + "; charset=UTF-8");
            heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes()); // 3
            heads.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            ctx.write(response);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelReadComplete");
            super.channelReadComplete(ctx);
            ctx.flush(); // 4
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("exceptionCaught");
            if(null != cause) {
                cause.printStackTrace();
            }
            if(null != ctx) {
                ctx.close();
            }
        }
    }
}
