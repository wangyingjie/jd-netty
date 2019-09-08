package com.jd.netty.rpc.consumer;

import com.jd.netty.rpc.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcClientProxy {


    public static <T> T getProxy(Class<T> clazz) {

        MethodProxy methodProxy = new MethodProxy(clazz);

        Class<?>[] interfaces = clazz.isInterface() ? new Class<?>[]{clazz} : clazz.getInterfaces();

        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, methodProxy);

        return result;
    }

    private static class MethodProxy implements InvocationHandler {

        private Class<?> clazz = null;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Object result = null;

            //如果传进来是一个已实现的具体类（本次演示略过此逻辑)
            if (Object.class.equals(method.getDeclaringClass())) {
                result = method.invoke(this, args);
            } else {
                result = rpcInvoker(proxy, method, args);
            }

            System.out.println("result====>" + result);

            return result;
        }

        private Object rpcInvoker(Object proxy, Method method, Object[] args) {

            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setParameterType(method.getParameterTypes());
            msg.setParameterValues(args);

            final RpcProxyHandler consumerHandler = new RpcProxyHandler();

            NioEventLoopGroup loopGroup = new NioEventLoopGroup();

            Bootstrap bootstrap = new Bootstrap();

            try {
                bootstrap.group(loopGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                //自定义协议解码器
                                /** 入参有5个，分别解释如下
                                 maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                                 lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                                 lengthFieldLength：长度字段的长度：如：长度字段是int型表示，那么这个值就是4（long型就是8）
                                 lengthAdjustment：要添加到长度字段值的补偿值
                                 initialBytesToStrip：从解码帧中去除的第一个字节数
                                 */
                                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                //自定义协议编码器
                                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                                //对象参数类型编码器
                                pipeline.addLast("encoder", new ObjectEncoder());
                                //对象参数类型解码器
                                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                pipeline.addLast("handler", consumerHandler);

                            }
                        });

                ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
                channelFuture.channel().writeAndFlush(msg).sync();
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                loopGroup.shutdownGracefully();
            }

            return consumerHandler.getResponse();
        }

        private class RpcProxyHandler extends ChannelInboundHandlerAdapter {

            private Object response;

            public Object getResponse() {
                return response;
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                System.out.println("msg==============>" + msg);

                response = msg;
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                System.out.println("client exception is general");
            }
        }


    }
}
