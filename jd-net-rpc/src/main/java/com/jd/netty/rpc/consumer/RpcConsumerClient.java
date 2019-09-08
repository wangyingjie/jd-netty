package com.jd.netty.rpc.consumer;

import com.jd.netty.rpc.api.IRpcHelloService;

public class RpcConsumerClient {

    public static void main(String[] args) {

        //IRpcHelloService service = RpcClientProxy.getProxy(IRpcHelloService.class);
        IRpcHelloService service = RpcClientProxy.getProxy(IRpcHelloService.class);

        service.sayHello("zhang san ");

    }
}
