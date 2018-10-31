package com.zhiyou100.doccloud.job.callback;

import lombok.Data;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 用于封装回调信息、将此信息hadoop序列化
 */
@Data
public class DocJobResponse implements Writable {
    //文档的id
    private int docJobId;
    //回调信息
    private String message;
    //状态是否成功
    private boolean success;
    //页数
    private int numOfPage;
    //重试次数
    private int retryTime;
    //完成时间
    private long finishTime;

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(docJobId);
        out.writeUTF(message);
        out.writeBoolean(success);
        out.writeInt(numOfPage);
        out.writeInt(retryTime);
        out.writeLong(finishTime);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        docJobId=in.readInt();
        message=in.readUTF();
        success=in.readBoolean();
        numOfPage=in.readInt();
        retryTime=in.readInt();
        finishTime=in.readLong();
    }
}
