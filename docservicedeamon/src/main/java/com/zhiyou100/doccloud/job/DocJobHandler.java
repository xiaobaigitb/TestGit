package com.zhiyou100.doccloud.job;

import com.zhiyou100.doccloud.job.callback.DocJobCallBack;
import com.zhiyou100.doccloud.job.callback.DocJobResponse;
import com.zhiyou100.doccloud.utils.FullTextIndexUtil;
import com.zhiyou100.doccloud.utils.HdfsUtil;
import com.zhiyou100.doccloud.utils.PdfUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;


import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;


/**
 * 功能的核心之一
 * 用于将文件冲hdfs上下载到本地，再将文件格式转化成HTML
 * 转成PDF格式，用于提取页码、提取首页缩略图、
 */
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
        //System.out.println("tmpWorkDirPath: "+tmpWorkDirPath);
        //1.3下载文件到临时目录
        try {
            HdfsUtil.copyToLocal(input,tmpWorkDirPath);
            log.info("download file to {}",tmpWorkDirPath);
            //step1:将下载到本地的文件格式转化成HTML
            convertToHtml(docJob.getFileName(),tmpWorkDir);
            //step2 转换成pdf
            convertToPdf(docJob.getFileName(),tmpWorkDir);
            Thread.sleep(500);
            log.info("sleep for a while");
            //获取pfd文件的路径
            String pdfPath=tmpWorkDir.getAbsolutePath()+"/"+docJob.getFileName().substring(0,docJob.getFileName().indexOf("."))+".pdf";
            log.info("pdfpath:{}",pdfPath);
            String htmlPath=tmpWorkDir.getAbsolutePath()+"/"+docJob.getFileName().substring(0,docJob.getFileName().indexOf("."))+".html";
            String thumbnailsPath=tmpWorkDir.getAbsolutePath()+"/"+docJob.getFileName().substring(0,docJob.getFileName().indexOf("."))+".png";
            log.info("thumbnailsPath: {}",thumbnailsPath);
            //step3 提取页码
            int numberOfPages = PdfUtil.getNumberOfPages(pdfPath);
            log.info("number of pages: {}",numberOfPages);
            //step4 提取首页缩略图
            PdfUtil.getThumbnails(pdfPath,thumbnailsPath);
            log.info("number of pages: {}",thumbnailsPath);
            //step5 利用solr建立索引
            //获取文章内容
            String content = PdfUtil.getContent(pdfPath);
            //创建文档对象-索引字段
            DocIndex docIndex = new DocIndex();
            //设置文档内容-索引字段
            docIndex.setDocContent(content);
            //设置文档名--索引字段
            docIndex.setDocName(docJob.getFileName());
            //设置文档id--没有用
            docIndex.setId(docJob.getDocId());
            //设置文档在hdfs上的路径--不建立索引
            docIndex.setUrl(tmpWorkDir.getAbsolutePath()+"/"+docJob.getFileName());
            //设置文档类型(文档后缀)--索引字段
            //获取文档后缀并设置
            String[] strings = docJob.getFileName().split("\\.");
            docIndex.setDocType(strings[1]);

            //利用solr开始创建索引
            FullTextIndexUtil.add(docIndex);
            log.info("doc index: {}",docIndex);

            //step6 上传文档-HTML、PDF、png
            HdfsUtil.copyFromLocal(htmlPath,docJob.getOutput());
            log.info("upload {} to hdfs:", htmlPath);
            HdfsUtil.copyFromLocal(pdfPath,docJob.getOutput());
            log.info("upload {} to hdfs:", pdfPath);
            HdfsUtil.copyFromLocal(thumbnailsPath,docJob.getOutput());
            log.info("upload {} to hdfs:", thumbnailsPath);
            //step7 清理临时目录
            log.info("start clear tmpworkdir : {}",tmpWorkDir.getAbsolutePath());
            //deleteTmpWorkDir(tmpWorkDir);
            //TODO：删除之前要睡一下，不然有可能不能删除干净。
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //java自带的delete不支持递归删除，使用common的工具类删除
            FileUtils.deleteDirectory(tmpWorkDir);
            log.info("delete tmpworkdir {} success",tmpWorkDir.getAbsolutePath());
            //step8 任务成功回调
            reportDocJob(numberOfPages,docJob,true,"success");
            log.info("start report docJob: {}",docJob.getName());
        } catch (Exception e) {
            //e.printStackTrace();
            //任务执行失败处理
            try {
                reportDocJob(0,docJob,false,e.getMessage());
            } catch (IOException e1) {
                //任务回调失败-记录日志
                log.info("report docjob {} failed to web:{}",e1.getMessage());
            }
        }
    }
    /**
     * 将报告job成功与否
     * @param numberOfPages
     * @param docJob
     * @param success
     * @param message
     */
    private void reportDocJob(int numberOfPages, DocJob docJob, boolean success, String message) throws IOException {
        //创建一个回调因袭对象
        DocJobResponse docJobResponse = new DocJobResponse();
        //设置id
        docJobResponse.setDocJobId(docJob.getId());
        //设置返回信息
        docJobResponse.setMessage(message);
        //设置文件页数
        docJobResponse.setNumOfPage(numberOfPages);
        //设置任务执行的结果
        docJobResponse.setSuccess(success);
        //判断任务是否成功-如果成功就不尝试
        if (!success){
            docJobResponse.setRetryTime(1);
        }
        //设置结束时间--当前时间
        docJobResponse.setFinishTime(System.nanoTime());
        //通过RPC通信发出请求
        DocJobCallBack jobCallback = RPC.getProxy(DocJobCallBack.class, DocJobCallBack.versionID, new InetSocketAddress("localhost", 8877), new Configuration());
        log.info("report job:{} to web : {}",docJob,docJobResponse);
        //将回调信息发送到服务器
        jobCallback.reportDocJob(docJobResponse);
    }

    private void convertToHtml(String fileName, File tmpWorkDir) throws IOException {
        String command = "D:\\soft\\LibreOffice_6.0.6\\program\\soffice --headless --invisible --convert-to html " + fileName;
        Process process = Runtime.getRuntime().exec(command, null, tmpWorkDir);
        //结果信息
        log.info("convert to html stdout:{}",IOUtils.toString(process.getInputStream()));
        //错误信息
        log.info("convert to html stderr:{}",IOUtils.toString(process.getErrorStream()));
    }

    private void convertToPdf(String fileName, File tmpWorkDir) throws IOException {
        String command = "D:\\soft\\LibreOffice_6.0.6\\program\\soffice --headless --invisible --convert-to pdf " + fileName;
        Process process = Runtime.getRuntime().exec(command, null, tmpWorkDir);
        //结果信息
        log.info("convert to html stdout:{}",IOUtils.toString(process.getInputStream()));
        //错误信息
        log.info("convert to html stderr:{}",IOUtils.toString(process.getErrorStream()));
    }
}
