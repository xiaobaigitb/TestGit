package com.zhiyou100.doccloud;

import com.zhiyou100.doccloud.job.callback.DocJobCallBack;
import com.zhiyou100.doccloud.job.callback.DocJobCallBackImpl;
import com.zhiyou100.doccloud.util.SpringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;

@SpringBootApplication//<--容器外部
public class DoccloudwebApplication {
    //容器外部
    public static void main(String[] args) throws IOException {
        SpringApplication.run(DoccloudwebApplication.class, args);
        //暴漏服务端端口
        startDocJobCallBack();
    }

    /**
     * 暴漏服务端端口
     */
    private static void startDocJobCallBack() throws IOException {
        //创建服务端接口实现类对象
        //DocJobCallBackImpl instance = new DocJobCallBackImpl();
        //从容器获取容器内部bean
        DocJobCallBackImpl instance = SpringUtil.getApplicationContext().getBean(DocJobCallBackImpl.class);
        //开启线程
        //new Thread(instance).start();

        // 创建一个RPC builder
        RPC.Builder builder = new RPC.Builder(new Configuration());

        //指定RPC Server的参数
        builder.setBindAddress("localhost");
        builder.setPort(8877);

        //将自己的程序部署到server上
        builder.setProtocol(DocJobCallBack.class);
        builder.setInstance(instance);

        //创建Server
        RPC.Server server = builder.build();

        //启动服务
        server.start();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //单个文件最大
        factory.setMaxFileSize("102400KB"); //KB,MB
        /// 设置总上传数据总大小
        factory.setMaxRequestSize("102400KB");
        return factory.createMultipartConfig();
    }
}
