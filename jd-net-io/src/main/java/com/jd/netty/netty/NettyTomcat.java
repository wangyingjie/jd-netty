package com.jd.netty.netty;

import com.jd.netty.netty.http.JDNetServlet;
import com.jd.netty.netty.http.JDRequest;
import com.jd.netty.netty.http.JDResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class NettyTomcat {

    private static final int PORT = 8080;

    private ServerSocket serverSocket;

    private Map<String, JDNetServlet> servletMapping = new HashMap<>();

    private Properties webXml = new Properties();

    private final String WEB_INF = this.getClass().getResource("/").getPath() + "WEB-INF/";

    public void init() {
        try {
            FileInputStream fis = new FileInputStream(WEB_INF + "web-net.properties");

            webXml.load(fis);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Objects.isNull(webXml)) {
            throw new RuntimeException("webXml is null!");
        }

        // 循环webXml
        webXml.forEach((k, v) -> {
            String key = k.toString();
            if (key.endsWith(".url")) {
                String url = webXml.getProperty(k + "");
                String className = webXml.getProperty(key.replace(".url", ".className"));

                System.out.println(url + "-----netty----" + className);

                JDNetServlet servlet = null;
                try {
                    servlet = (JDNetServlet) Class.forName(className).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                servletMapping.put(url, servlet);
            }
        });
    }


    public void start() {

        // 加载配置文件
        init();

        // Boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        // worker线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // Netty 服务类
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    // 主线程处理类,看到这样的写法，底层就是用反射
                    .channel(NioServerSocketChannel.class)

                    // 子线程处理类 , Handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 客户端初始化处理
                        @Override
                        protected void initChannel(SocketChannel client) throws Exception {
                            // 无锁化串行编程
                            //Netty对HTTP协议的封装，顺序有要求
                            // HttpResponseEncoder 编码器
                            client.pipeline().addLast(new HttpResponseEncoder());

                            // HttpRequestDecoder 解码器
                            client.pipeline().addLast(new HttpRequestDecoder());

                            // 业务逻辑处理
                            client.pipeline().addLast(new JDTomcatHandler());
                        }
                    })
                    // 针对主线程的配置 分配线程最大数量 128
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 针对子线程的配置 保持长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(PORT).sync();

            System.out.println("bio tomcat starting " + PORT);

            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class JDTomcatHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;
                // 转交给我们自己的request实现
                JDRequest request = new JDRequest(ctx, req);
                // 转交给我们自己的response实现
                JDResponse response = new JDResponse(ctx, req);
                // 实际业务处理
                String url = request.getUrl();

                if (servletMapping.containsKey(url)) {
                    servletMapping.get(url).service(request, response);
                } else {
                    response.write("404 - Not Found");
                }

            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        }
    }


    public static void main(String[] args) throws Exception {

        NettyTomcat tomcat = new NettyTomcat();

        tomcat.start();

        System.out.println(tomcat.WEB_INF + "\n" + tomcat.webXml);
        System.out.println(tomcat.servletMapping);
    }
}
