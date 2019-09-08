package com.jd.netty.bio.servlet;

import com.jd.netty.bio.http.JDServlet;
import com.jd.netty.bio.http.Request;
import com.jd.netty.bio.http.Response;

import java.io.IOException;

public class FirstServlet extends JDServlet {

    @Override
    protected void doPost(Request request, Response response) throws IOException {


        response.write("first servlet");
    }

    @Override
    protected void doGet(Request request, Response response) {
        this.doGet(request, response);
    }
}
