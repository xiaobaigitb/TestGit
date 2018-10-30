package com.zhiyou100.doccloud.doccloudweb.util;

import com.google.common.io.Resources;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/*
*@ClassName:HdfsUtil
 @Description:TODO
 @Author:
 @Date:2018/10/29 17:17 
 @Version:v1.0
*/
public class HdfsUtil {
    //文档上传工具类
    public static void upload(byte[] src, String docName, String dst) throws IOException {
        //加载配置文件
        Configuration coreSiteConf = new Configuration();
        coreSiteConf.addResource(Resources.getResource("core-site.xml"));
        //获取文件系统客户端对象
        FileSystem fileSystem = FileSystem.get(coreSiteConf);

        FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path(dst + "/" + docName));

        fsDataOutputStream.write(src);
        fsDataOutputStream.close();
        fileSystem.close();
    }
}
