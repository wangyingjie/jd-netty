package com.jd.netty.rpc.registry;

import com.jd.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHandler extends ChannelInboundHandlerAdapter {

    // 1、根据一个包名将所有的class扫描出来，放到一个容器中
    // 2、给每一个class取一个唯一的名字，作为服务名称，保存到容器中
    // 3、客户端连接时则可以获取到 InvokerProtocol对象
    // 4、要找到符合条件的服务
    // 4、通过远程调用得到返回结果，并将结果返回给客户端

    private List<String> clazzList = new ArrayList<>();

    private ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();


    public RegistryHandler() {
        scannerClazz("com.jd.netty.rpc.provider");
        doRegister();
    }

    private void doRegister() {
        if (clazzList.size() == 0) {
            return;
        }
        clazzList.forEach(clazz -> {
            try {
                Class<?> aClass = Class.forName(clazz);

                // 获取到接口
                Class<?> interfaces = aClass.getInterfaces()[0];

                map.put(interfaces.getName(), aClass.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println("register map is :\n" + map);
    }

    public void scannerClazz(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dirFile = new File(url.getFile());

        for (File file : dirFile.listFiles()) {
            if (file.isDirectory()) {
                scannerClazz(packageName + "." + file.getName());
            } else {
                String classFileName = file.getName();
                String clazzFullName = packageName + "." + classFileName.replace(".class", "");
                clazzList.add(clazzFullName);
            }
        }

        System.out.println("scanner classes is :\n" + clazzList);
    }


    //有客户端连接的时候回调
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol) msg;

        if (map.containsKey(request.getClassName())) {
            Object clazz = map.get(request.getClassName());
            // 根据方法名称及参数列表类型获取目标方法
            Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParameterType());
            // 通过反射执行对应的方法
            result = method.invoke(clazz, request.getParameterValues());
        }

        ctx.write(result);
        ctx.flush();
        ctx.close();

    }

    //连接发生异常的时候回调
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
