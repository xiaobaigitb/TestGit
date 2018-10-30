package com.zhiyou100.doccloud.job;

import com.zhiyou100.doccloud.utils.HdfsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class DocJobHandler implements Runnable {
    private DocJob docJob;

    public DocJobHandler(DocJob docJob) {
        this.docJob = docJob;
        log.info("start to deal job {}",docJob);
    }

    /**
     *将文件冲hdfs上下载到本地，再将文件格式转化成HTML，最终上传到hdfs上
     */
    @Override
    public void run() {
        //1.将hdfs上的文件下载到本地
        //1.1获取文件的下载路径（在hdfs上的位置）
        String input = docJob.getInput();
        //1.2创建目标路径（下载到本地的路径）
        String tmpWorkDirPath = "/tmp/docjobdaemon/" + UUID.randomUUID().toString() + "/";
        File tmpWorkDir = new File(tmpWorkDirPath);
        tmpWorkDir.mkdirs();
        System.out.println("tmpWorkDirPath: "+tmpWorkDirPath);
        //1.3下载文件到临时目录
        try {
            HdfsUtil.copyToLocal(input,tmpWorkDirPath);
            log.info("download file to {}",tmpWorkDirPath);
            //step1:将下载到本地的文件格式转化成HTML
            String command = "D:\\soft\\LibreOffice_6.0.6\\program\\soffice --headless --invisible --convert-to html " + docJob.getFileName();
            Process process = Runtime.getRuntime().exec(command, null, tmpWorkDir);
            //结果信息
            System.out.println(IOUtils.toString(process.getInputStream()));
            //错误信息
            System.out.println(IOUtils.toString(process.getErrorStream()));
            //step2 转换成pdf
            //step3 提取页码
            //step4 提取首页缩略图
            //step5 利用solr建立索引
            //step6 上传结果
            //step7 清理临时目录
            //step8 任务成功回调
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
