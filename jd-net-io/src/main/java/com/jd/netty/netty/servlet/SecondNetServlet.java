package com.jd.netty.netty.servlet;

import com.jd.netty.netty.http.JDNetServlet;
import com.jd.netty.netty.http.JDRequest;
import com.jd.netty.netty.http.JDResponse;

import java.io.IOException;

public class SecondNetServlet extends JDNetServlet {

    @Override
    protected void doPost(JDRequest request, JDResponse response) throws IOException {

        String url = request.getUrl();

        response.write("This is Second Servlet");
    }

    @Override
    protected void doGet(JDRequest request, JDResponse response) throws IOException {
        this.doPost(request, response);
    }
}
