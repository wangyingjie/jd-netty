package com.jd.netty.bio;

import com.jd.netty.bio.http.JDServlet;
import com.jd.netty.bio.http.Request;
import com.jd.netty.bio.http.Response;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class BIOTomcat {

    private static final int PORT = 8080;

    private ServerSocket serverSocket;

    private Map<String, JDServlet> servletMapping = new HashMap<>();

    private Properties webXml = new Properties();

    private final String WEB_INF = this.getClass().getResource("/").getPath() + "WEB-INF/";

    public void init() {
        try {
            FileInputStream fis = new FileInputStream(WEB_INF + "web.properties");

            webXml.load(fis);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Objects.isNull(webXml)) {
            throw new RuntimeException("webXml is null!");
        }

        // 循环webXml
        webXml.forEach((k, v) -> {
            String key = k.toString();
            if (key.endsWith(".url")) {
                String url = webXml.getProperty(k + "");
                String className = webXml.getProperty(key.replace(".url", ".className"));

                System.out.println(url + "-----rrrr----" + className);

                JDServlet servlet = null;
                try {
                    servlet = (JDServlet) Class.forName(className).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                servletMapping.put(url, servlet);
            }
        });
    }


    public void start() {

        // 加载配置文件
        init();

        try {
            serverSocket = new ServerSocket(PORT);

            System.out.println("bio tomcat starting " + PORT);

            // 一个死循环等待用户请求上来
            while (true) {
                Socket client = serverSocket.accept();

                process(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void process(Socket client) throws IOException {

        InputStream inputStream = client.getInputStream();
        Request request = new Request(inputStream);

        OutputStream outputStream = client.getOutputStream();
        Response response = new Response(outputStream);

        String url = request.getUrl();
        if (servletMapping.containsKey(url)) {

            JDServlet servlet = servletMapping.get(url);
            servlet.service(request, response);

        } else {
            response.write("url: " + url + " Not Found!");
        }


        outputStream.close();
        inputStream.close();
    }

    public static void main(String[] args) throws Exception {

        BIOTomcat tomcat = new BIOTomcat();

        tomcat.start();

        System.out.println(tomcat.WEB_INF + "\n" + tomcat.webXml);
        System.out.println(tomcat.servletMapping);
    }
}
