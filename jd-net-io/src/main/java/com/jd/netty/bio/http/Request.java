package com.jd.netty.bio.http;

import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Data
public class Request {


    private Map<String, Object> parameter = new HashMap<>();

    private InputStream inputStream;

    private String url;

    private String method;


    public Request(InputStream inputStream) {
        this.inputStream = inputStream;

        try {
            String content = "";

            byte[] buffer = new byte[2048];

            if (inputStream.read(buffer) > 0) {
                content = new String(buffer, "UTF-8");
            }

            System.out.println(content);

            String[] line = content.split("\\n");

            Arrays.stream(line).forEach(System.out::println);

            String httpInfo = line[0];

            String[] split = httpInfo.split("\\s");

            System.out.println("======>" + split + "===========" + split.length);

            Arrays.stream(split).forEach(System.out::println);

            this.method = split[0];


            if (split.length > 1) {
                int paramIndex = split[1].indexOf("?");

                if (paramIndex > 0) {

                    this.url = split[1].substring(0, paramIndex);

                    String paramStr = split[1].substring(paramIndex + 1);
                    String[] split1 = paramStr.split("&");
                    Arrays.stream(split1).forEach(str -> {
                        String[] param = str.split("=");
                        parameter.put(param[0], param[1]);
                    });

                } else {
                    this.url = split[1];
                }

            } else {// 无参数
                this.url = split[0];
                System.out.println("xxxxxxxxxxxxxx=" + split[0]);
            }


            System.out.println(method + "============" + url + "=============" + parameter);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
