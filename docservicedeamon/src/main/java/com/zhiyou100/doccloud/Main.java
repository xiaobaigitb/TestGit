package com.zhiyou100.doccloud;

import com.zhiyou100.doccloud.job.JobDaemonService;
import com.zhiyou100.doccloud.job.JobDaemonServiceImpl;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;

/**
 * 守护进程--项目的入口类
 * 3.服务端：暴露端口
 */
public class Main {
    public static void main(String[] args) throws IOException {
        //创建服务端接口实现类对象
        JobDaemonServiceImpl instance = new JobDaemonServiceImpl();
        //开启线程
        new Thread(instance).start();

        // 创建一个RPC builder
        RPC.Builder builder = new RPC.Builder(new Configuration());

        //指定RPC Server的参数
        builder.setBindAddress("localhost");
        builder.setPort(7788);

        //将自己的程序部署到server上
        builder.setProtocol(JobDaemonService.class);
        builder.setInstance(instance);

        //创建Server
        RPC.Server server = builder.build();

        //启动服务
        server.start();
    }
}
