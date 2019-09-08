package com.jd.netty.rpc.provider;

import com.jd.netty.rpc.api.IRpcService;

public class IRpcServiceImpl implements IRpcService {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        return a - b;
    }

    @Override
    public int muli(int a, int b) {
        return a * b;
    }

    @Override
    public int div(int a, int b) {
        return a / b;
    }

}
