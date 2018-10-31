package com.zhiyou100.doccloud.job.callback;

import org.apache.hadoop.ipc.VersionedProtocol;

/**
 * 作为信息回调的服务端
 * 1.服务器定义接口继承VersionedProtocol
 */
public interface DocJobCallBack extends VersionedProtocol {
    //暗号
    long versionID=2L;
    //定义方法-返回信息
    void reportDocJob(DocJobResponse docJobResponse);
}
