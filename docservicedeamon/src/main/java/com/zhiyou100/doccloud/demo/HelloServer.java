package com.zhiyou100.doccloud.demo;

import org.apache.hadoop.ipc.VersionedProtocol;

/**
 * 1.服务器定义接口
 */
public interface HelloServer extends VersionedProtocol {
    //版本号--用于通信暗号
    long versionID= 123L;
    String sayHello(String name);
}
