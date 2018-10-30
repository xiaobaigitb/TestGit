package com.zhiyou100.doccloud.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HelloClient {
    public static void main(String[] args) throws IOException {
        /**
         * HelloServer:服务端接口类
         * 123L：通信发暗号
         * InetSocketAddress：服务端地址
         * Configuration：
         */
        HelloServer proxy = RPC.getProxy(HelloServer.class, 123L, new InetSocketAddress("localhost", 7788), new Configuration());
        String result = proxy.sayHello("tom");
        System.out.println("结果是： "+result);

    }
}
