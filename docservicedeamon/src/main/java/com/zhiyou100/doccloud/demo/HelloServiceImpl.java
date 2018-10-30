package com.zhiyou100.doccloud.demo;

import org.apache.hadoop.ipc.ProtocolSignature;

import java.io.IOException;

/**
 * 2.定义接口实现类
 */
public class HelloServiceImpl implements HelloServer {
    public String sayHello(String name) {
        return "hello： "+name;
    }

    public long getProtocolVersion(String s, long l) throws IOException {
        return versionID;
    }

    public ProtocolSignature getProtocolSignature(String s, long l, int i) throws IOException {
        return null;
    }
}
