package com.zhiyou100.doccloud.job;

import org.apache.hadoop.ipc.VersionedProtocol;

/**
 * 定义接口继承VersionedProtocol
 */
public interface JobDaemonService extends VersionedProtocol {
    //定义通信间的暗号
    long versionID=1L;
    //定义提交方法
    void submitDocJob(DocJob job);

}
