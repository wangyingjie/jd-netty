package com.jd.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class InvokerProtocol implements Serializable {

    private String className;
    private String methodName;

    /**
     * 参数类型列表
     */
    private Class<?>[] parameterType;

    /**
     * 参数值列表
     */
    private Object[] parameterValues;

}
