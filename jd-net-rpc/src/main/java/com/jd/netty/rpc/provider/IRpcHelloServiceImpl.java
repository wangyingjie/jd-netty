package com.jd.netty.rpc.provider;

import com.jd.netty.rpc.api.IRpcHelloService;

public class IRpcHelloServiceImpl implements IRpcHelloService {

    @Override
    public String sayHello(String content) {
        System.out.println("coming****************" + content);
        return "Hello " + content;
    }
}
