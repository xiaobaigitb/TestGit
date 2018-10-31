package com.zhiyou100.doccloud.job;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

/**
 * 用来封装索引字段
 *  @Field：索引的字段添加这个注解
 */
@Data
public class DocIndex {
    @Field
    //文章id--自带，因为数据库要有，但是用不到
    private String id;
    @Field
    //文章名--添加
    private String docName;
    @Field
    //文章URL--不用,为了查询数据
    private String url;
    @Field
    //文章内容--有需要修改
    private String docContent;
    @Field
    //文章类型--没有，文本普通
    private String docType;
}
