package com.zhiyou100.doccloud.job.callback;

/**
 * 作为信息回调的客户端
 */
public interface DocJobCallBack {
    //暗号
    long versionID=2L;
    //定义方法-返回信息
    void reportDocJob(DocJobResponse docJobResponse);
}
