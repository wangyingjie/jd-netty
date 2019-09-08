package com.jd.netty.netty.http;

import java.io.IOException;

public abstract class JDNetServlet {

    public void service(JDRequest request, JDResponse response) throws IOException {

        if (request.getMethod().equals("doGet")) {
            doGet(request, response);
        } else {
            doPost(request, response);
        }

    }

    protected abstract void doPost(JDRequest request, JDResponse response) throws IOException;

    protected abstract void doGet(JDRequest request, JDResponse response) throws IOException;

}
