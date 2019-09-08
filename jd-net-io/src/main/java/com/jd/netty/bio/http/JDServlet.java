package com.jd.netty.bio.http;

import java.io.IOException;

public abstract class JDServlet {

    public void service(Request request, Response response) throws IOException {

        if (request.getMethod().equals("doGet")) {
            doGet(request, response);
        } else {
            doPost(request, response);
        }

    }

    protected abstract void doPost(Request request, Response response) throws IOException;

    protected abstract void doGet(Request request, Response response) throws IOException;

}
