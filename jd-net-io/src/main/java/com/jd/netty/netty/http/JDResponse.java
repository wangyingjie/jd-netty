package com.jd.netty.netty.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.UnsupportedEncodingException;

public class JDResponse {

    //SocketChannel的封装
    private ChannelHandlerContext ctx;

    private HttpRequest req;

    public JDResponse(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public void write(String s) {

        try {
            if (s == null || s.length() == 0) {
                return;
            }


            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(s.getBytes("UTF-8"))
            );

            response.headers().set("Content-Type", "text/html;");

            ctx.write(response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            // 一定要有flush
            ctx.flush();
            ctx.close();
        }


    }
}
