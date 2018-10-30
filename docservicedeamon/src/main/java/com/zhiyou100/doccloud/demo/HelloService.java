package com.zhiyou100.doccloud.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;

/**
 * 暴露接口
 */
public class HelloService {
    public static void main(String[] args) throws IOException {
        HelloServiceImpl instance = new HelloServiceImpl();
        // 创建一个RPC builder
        RPC.Builder builder = new RPC.Builder(new Configuration());

        //指定RPC Server的参数
        builder.setBindAddress("localhost");
        builder.setPort(7788);

        //将自己的程序部署到server上
        builder.setProtocol(HelloServer.class);
        builder.setInstance(instance);

        //创建Server
        RPC.Server server = builder.build();

        //启动服务
        server.start();
        System.out.println("server is start");
    }
}
