package com.zhiyou100.doccloud.doccloudweb.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 文件属性
 */
@Entity
@Table(name = "doc") //映射到数据库中的表
@Data //get/set
public class Doc {

    @Id //主键
    //告诉框架id生成策略（怎么生成）GenerationType.IDENTITY：表示自动生成
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "md5")//如果数据库字段与entity中字段名一样，则不用加此注解
    private String md5;
    @Column(name = "doc_name")
    private String docName;
    @Column(name = "doc_type")
    private String docType;
    @Column(name = "doc_status")
    private String docStatus;
    @Column(name = "doc_size")
    private int docSize;
    @Column(name = "doc_dir")
    private String docDir;
    @Column(name = "user_id")
    private int userId;
    @Column(name = "doc_create_time")
    private Date docCreateTime;
    @Column(name = "doc_comment")
    private String docComment;
    @Column(name = "doc_permission")
    private String docPermission;

}
