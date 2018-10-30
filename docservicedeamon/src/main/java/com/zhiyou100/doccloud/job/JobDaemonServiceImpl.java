package com.zhiyou100.doccloud.job;

import com.zhiyou100.doccloud.utils.BdbPersistentQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.ipc.ProtocolSignature;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 2.定义接口的实现类
 * 实现Runnable接口：是为了使用多线程处理
 */
@Slf4j
public class JobDaemonServiceImpl implements JobDaemonService,Runnable{
    //定义将hdfs下载到本地的目录的根路径
    private static final String WORK_DIR="/tmp/docjobdaemon/";
    //定义持久化对象
    public  BdbPersistentQueue<DocJob> queue;
    //定义线程池--多线程并行处理
    private ExecutorService pool = Executors.newFixedThreadPool(4);
    //定义一个标准-让线程运行
    private boolean flag = true;

    //构造方法：用于创建berkly数据库目录，并初始化持久化队列
    public JobDaemonServiceImpl(){
        //创建工作目录--本地保存路径
        File workDir = new File(WORK_DIR + "/" + "bdb/");
        if (!workDir.exists()){
            //如果不存在将创建
            workDir.mkdirs();
            System.out.println(workDir.getAbsolutePath());
        }
        //初始化持久化队列
        queue = new BdbPersistentQueue<DocJob>(WORK_DIR+"/"+"bdb/", "docjob", DocJob.class);
    }

    public void submitDocJob(DocJob job) {
        System.out.println(job);
        //将任务保存在序列化队列中，1.保证任务不丢失   2.并发控制，内存溢出
        log.info("receive job {}",job);
        queue.offer(job);
    }

    public long getProtocolVersion(String s, long l) throws IOException {
        return versionID;
    }

    public ProtocolSignature getProtocolSignature(String s, long l, int i) throws IOException {
        return null;
    }

    @Override
    public void run() {
        while (flag){
            //将任务从序列化队列中取出任务,poll:每取出一个就从磁盘中移除一个
            DocJob docJob = queue.poll();
            //判断docjob中否为空
            if (docJob==null){
                //为空，等待5000毫秒
                try {
                    Thread.sleep(5000);
                    System.out.println("waiting for docjob");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                pool.submit((Runnable) new DocJobHandler(docJob));
            }
        }
    }
}
