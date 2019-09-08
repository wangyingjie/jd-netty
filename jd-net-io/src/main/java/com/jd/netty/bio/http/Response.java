package com.jd.netty.bio.http;

import java.io.IOException;
import java.io.OutputStream;

public class Response {


    private OutputStream outputStream;

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String content) throws IOException {

        StringBuffer sb = new StringBuffer();

        sb.append("HTTP/1.1 200 OK \n");
        sb.append("Content-Type: text/html; \n");
        sb.append("\r\n");

        sb.append(content);

        outputStream.write(sb.toString().getBytes());
    }


}
