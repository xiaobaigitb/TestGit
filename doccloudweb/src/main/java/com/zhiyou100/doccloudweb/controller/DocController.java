package com.zhiyou100.doccloudweb.controller;

import com.zhiyou100.doccloudweb.dao.DocRepository;

import com.zhiyou100.doccloudweb.entity.Doc;
import com.zhiyou100.doccloudweb.service.DocService;
import com.zhiyou100.doccloudweb.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller  //表示是controller层--业务层
@RequestMapping("/doc")
@Slf4j
public class DocController {

    @Autowired
    private DocService docService;

    //定义合法的文件后缀类型
    public static final String[] DOC_SUFFIXS= new String[]{"doc", "docx", "ppt", "pptx", "txt", "xls", "xlsx", "pdf"};
    //定义文件最大大小
    public static final int DOC_MAX_SIZE = 128*1024*1024;


    @RequestMapping("/doclist")
    @ResponseBody
    Doc doList(){
        Optional<Doc> id = docService.findById(1);
        return id.get();
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
