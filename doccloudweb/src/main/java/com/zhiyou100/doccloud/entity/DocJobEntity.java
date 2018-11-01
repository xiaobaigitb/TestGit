package com.zhiyou100.doccloud.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 用于封装文件上传任务的字段
 */
@Data
@Entity
@Table(name = "doc_job")
public class DocJobEntity {
    @Id
    private String id;

    private String name;
    @Column(name = "job_type")
    private String jobType;
    @Column(name = "user_id")
    private int userId;
    //提交时间
    @Column(name = "submit_time")
    private long submitTime;
    //完成时间
    @Column(name = "finish_time")
    private long finishTime;
    //任务状态
    @Column(name = "job_status")
    private String jobStatus;
    //任务重试次数
    @Column(name = "retry_time")
    private int retryTime;
    //文档输入路径
    private String input;
    //任务输出路径
    private String output;
    //任务处理文件名
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "doc_id")
    private int docId;

}
