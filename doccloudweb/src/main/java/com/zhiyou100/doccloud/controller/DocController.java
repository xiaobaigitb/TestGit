package com.zhiyou100.doccloud.controller;

import com.zhiyou100.doccloud.entity.DocJobEntity;
import com.zhiyou100.doccloud.service.DocJobService;
import com.zhiyou100.doccloud.service.DocService;
import com.zhiyou100.doccloud.util.HdfsUtil;
import com.zhiyou100.doccloud.util.MD5Util;
import com.zhiyou100.doccloud.entity.Doc;
import com.zhiyou100.doccloud.job.DocJob;
import com.zhiyou100.doccloud.job.DocJobType;
import com.zhiyou100.doccloud.job.JobDaemonService;
import com.zhiyou100.doccloud.job.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Controller  //表示是controller层--业务层
@RequestMapping("/doc")
@Slf4j
public class DocController {

    @Autowired
    private DocService docService;
    @Autowired
    private DocJobService docJobService;

    //定义合法的文件后缀类型
    public static final String[] DOC_SUFFIXS= new String[]{"doc", "docx", "ppt", "pptx", "txt", "xls", "xlsx", "pdf"};
    //定义文件最大大小
    public static final int DOC_MAX_SIZE = 128*1024*1024;
    //定义文件保存到hdfs上的根目录
    public static final String HOME="hdfs://192.168.228.13:9000/doccloud";


    @RequestMapping("/doclist")
    @ResponseBody
    Doc doList(){
        //Optional:是一个集合，可能空指针--需要判断
        Optional<Doc> doc = docService.findById(1);
        if (doc.isPresent()){
            return doc.get();
        }
        return null;
    }

    @RequestMapping("/view")
    @ResponseBody
    public String view(String md5) throws IOException {
        //通过MD5获取文件对象
        Optional<Doc> docOptional = docService.findByMd5(md5);
        //如果文件存在，这获取文件实体
        if (docOptional.isPresent()){
            Doc doc = docOptional.get();
            //获取文件名
            String docName = doc.getDocName();
            //获取文件在hdfs上的路径，没有文件名
            String docDir = doc.getDocDir();
            //制作PDF的文件名
            String viewName = docName.substring(0,docName.indexOf("."))+".pdf";
            //制作PDF的路径+名字
            String viewPath = docDir+"/"+viewName;
            log.info("view path:{}",viewPath);
            //获取执行类所在的目录
            String classPath = DocController.class.getClassLoader().getResource("").getPath();
            log.info("class path:{}",classPath);
            String relativeViewPath="/static/tmp/"+UUID.randomUUID().toString()+"/"+viewName;
            String tmpViewPath=classPath+"/static/tmp/"+UUID.randomUUID().toString()+"/"+viewName;
            HdfsUtil.download(viewPath,tmpViewPath);
            return relativeViewPath;
        }
        return null;
    }


    //方法级别映射，必须有，那么这个方法的访问地址就是/test/aaa，
    // 请求到的页面就是test.jsp【当然，这里的.jsp需要在配置文件中配置】
    @RequestMapping("/helloworld")
    // 表示该方法的返回结果直接写入 HTTP response body 中，
    // 一般在异步获取数据时使用【也就是AJAX】
    @ResponseBody
    String helloworld(){
        return "helloword";
    }



    @RequestMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file){
        //判断是否是文件
        if (file.isEmpty()){
            return "file is empty";
        }
        //获取文件名
        String filename = file.getOriginalFilename();
        //以点分割-获取文件后缀
        String[] strings = filename.split("\\.");
        if (strings.length==1){
            return "file does not has suffix";
        }
        String suffix = strings[1];
        log.info("doc suffix is {}",suffix);
        //1.判断文件后缀是否合法
        boolean flag = isSuffixLegal(suffix);
        if (!flag){
            return "file is illegal";
        }

        try {
            //2.判断文件大小是否合法
            byte[] bytes = file.getBytes();
            log.info("file size is {}",bytes.length);
            if (bytes.length>DOC_MAX_SIZE){
                return "file is large,file Max size:"+DOC_MAX_SIZE;
            }
            //3.计算文档的MD5值
            String md5 = getMD5(bytes);
            log.info("file is md5 {} ",md5);
            //用户上传文件，保存到数据库
            //1.校验数据库中的md5值，判断数据库中是否存在
            Optional<Doc> doc = docService.findByMd5(md5);
            if (doc.isPresent()){
                //2.如果存在，更新
                // 2.1获取文件对象
                Doc docEntity = doc.get();
                //2.2设置文件更新的人
                docEntity.setUserId(new Random().nextInt());
                //2.3保存到数据库
                docService.save(docEntity);
                log.info("update doc is: {}",docEntity);
            }else {
                //3.如果不存在,将文件元数据保存到数据库，将数据保存到hdfs
                //3.1保存数据到hdfs
                //3.1.1生成文件保存路径:HOME+当前时间
                String date = getDate();
                //String dst = HOME+"/"+date+"/"+file.getOriginalFilename()+"/";
                String dst=HOME+"/"+date+"/"+UUID.randomUUID().toString()+"/";
                log.info("file dst {}",dst);
                //3.1.2上传文件
                HdfsUtil.upload(bytes,file.getOriginalFilename(),dst);
                //3.2将元数据保存到数据库
                //3.2.1创建一个文件对象
                Doc docEntity = new Doc();
                //3.2.2设置作者
                docEntity.setUserId(new Random().nextInt());
                //3.2.3设置备注
                docEntity.setDocComment("hadoopClientCode");
                //3.2.4设置文件路径
                docEntity.setDocDir(dst);
                //3.2.5设置文件名
                docEntity.setDocName(filename);
                //3.2.6设置文件大小
                docEntity.setDocSize(bytes.length);
                //3.2.7设置文件权限
                docEntity.setDocPermission("1");
                //3.2.8设置文件类型（后缀）
                docEntity.setDocType(suffix);
                //3.2.9设置文件状态
                docEntity.setDocStatus("upload");
                //3.2.10设置文件的md5值--保证文件的唯一性
                docEntity.setMd5(md5);
                //3.2.11设置文件创作时间
                docEntity.setDocCreateTime(new Date());
                //3.2.12保存元数据
                Doc savedDoc = docService.save(docEntity);
                //上传成功以后需要提交文档转换任务
                DocJob docJob = submitDocJob(savedDoc, new Random().nextInt());
                //保存job信息
                saveDocJob(docJob);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            //将文件转化成字节数组
//            byte[] bytes = file.getBytes();
//            Path path = Paths.get("D:\\Desktop\\DocCloud\\upload\\" + file.getOriginalFilename());
//            Files.write(path,bytes);
//            log.info("upload file {} ",file.getOriginalFilename());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return "upload success";
    }

    /**
     *将文档任务实体保存到数据库中
     * @param docJob
     */
    private void saveDocJob(DocJob docJob) {
        //创建一个DocJob对象
        DocJobEntity docJobEntity = new DocJobEntity();
        //将客户端返回的id
        docJobEntity.setId(docJob.getId());
        docJobEntity.setJobStatus("running");
        docJobEntity.setSubmitTime(docJob.getSubmitTime());
        docJobEntity.setDocId(docJob.getDocId());
        docJobEntity.setInput(docJob.getInput());
        docJobEntity.setOutput(docJob.getOutput());
        docJobEntity.setName(docJob.getName());
        docJobEntity.setRetryTime(docJob.getRetryTime());
        docJobEntity.setJobType(docJob.getJobType().name());
        docJobEntity.setFileName(docJob.getFileName());
        docJobEntity.setUserId(docJob.getUserId());
        docJobService.save(docJobEntity);
    }
    /**
     * 提交任务到集群上运行--文档转换任务
     * @param docEntity
     * @param userId
     */
    private DocJob submitDocJob(Doc docEntity, int userId) throws IOException {
        //创建一个文档转换任务对象
        DocJob docJob = new DocJob();
        //1.设置提交者
        docJob.setUserId(userId);
        //2.设置任务名
        docJob.setName("doc convent");
        //3.任务的状态
        docJob.setJobStatus(JobStatus.SUBMIT);
        //4.设置任务类型
        docJob.setJobType(DocJobType.DOC_JOB_CONVERT);
        //5.设置提交时间
        docJob.setSubmitTime(System.nanoTime());
        //6.设置输入路径
        docJob.setInput(docEntity.getDocDir()+"/"+docEntity.getDocName());
        //7.设置输出路径
        docJob.setOutput(docEntity.getDocDir());
        //8.设置重试次数
        docJob.setRetryTime(4);
        //9.设置文件名
        docJob.setFileName(docEntity.getDocName());
        //10.设置job的uuid
        docJob.setId(UUID.randomUUID().toString());
        //11.文件的id
        docJob.setDocId(docEntity.getId());
        //todo 将job元数据保存到数据库
        //获取动态代理对象
        JobDaemonService jobDaemonService = RPC.getProxy(JobDaemonService.class, JobDaemonService.versionID, new InetSocketAddress("localhost", 7788), new Configuration());
        //提交任务到服务器（hdfs上）
        log.info("submit job:{}",docJob);
        jobDaemonService.submitDocJob(docJob);
        //将文档任务返回
        return docJob;
    }

    /**
     * 获取当前是时间，用于文件的保存路径
     * @return
     */
    private String getDate() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);

    }

    /**
     * 计算字节数组的MD5值
     * @param bytes
     * @return
     */
    private String getMD5(byte[] bytes) {
        return MD5Util.getMD5String(bytes);
    }

    /**
     * 判断文件后缀是否合法
     * @param suffix
     * @return
     */
    private boolean isSuffixLegal(String suffix) {
        for (String docsuffix :
                DOC_SUFFIXS) {
            if (suffix.equals(docsuffix)){
                return true;
            }
        }
        return false;
    }
}
