package com.zhiyou100.doccloud.job.callback;

import org.apache.hadoop.ipc.ProtocolSignature;

import java.io.IOException;

/**
 * 2.实自定义接口类
 */
public class DocJobCallBackImpl implements DocJobCallBack {
    @Override
    public void reportDocJob(DocJobResponse docJobResponse) {
        //todo
        //将修改数据库中job的状态
        System.out.println(docJobResponse);
    }

    @Override
    public long getProtocolVersion(String protocol, long clientVersion) throws IOException {
        return versionID;
    }

    @Override
    public ProtocolSignature getProtocolSignature(String protocol, long clientVersion, int clientMethodsHash) throws IOException {
        return null;
    }
}
