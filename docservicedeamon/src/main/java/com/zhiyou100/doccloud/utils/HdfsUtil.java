package com.zhiyou100.doccloud.utils;

import com.google.common.io.Resources;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;

public class HdfsUtil {
    public static final String HOME="hdfs://192.168.228.13:9000/";
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

    /**
     * 将本地文件上传到hdfs
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copyFromLocal(String src,String dst) throws IOException {
        Configuration coreSiteConf = new Configuration();
        coreSiteConf.addResource(Resources.getResource("core-site.xml"));
        //获取文件系统客户端对象
        FileSystem fileSystem = FileSystem.get(coreSiteConf);
        fileSystem.copyFromLocalFile(new Path(src),new Path(dst));
        fileSystem.close();
    }

    /**
     * 将集群的问价下载到本地
     * @param dst
     * @param localPath
     * @throws IOException
     */
    public static void copyToLocal(String dst,String localPath) throws IOException {
        Configuration conf = new Configuration();
        conf.addResource(Resources.getResource("core-site.xml"));
        FileSystem fs = FileSystem.get(URI.create(dst),conf);
        fs.copyToLocalFile(new Path(dst),new Path(localPath));
        fs.close();
    }
}
