package com.zhiyou100.doccloud.job.callback;

import com.zhiyou100.doccloud.entity.Doc;
import com.zhiyou100.doccloud.entity.DocJobEntity;
import com.zhiyou100.doccloud.service.DocJobService;
import com.zhiyou100.doccloud.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.ipc.ProtocolSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

/**
 * 2.实自定义接口类
 */
@Slf4j
@Component
@Transactional //两表修改，添加事务 TODO
public class DocJobCallBackImpl implements DocJobCallBack {
    @Autowired
    private DocJobService docJobService;
    @Autowired
    private DocService docService;
    @Override
    public void reportDocJob(DocJobResponse docJobResponse) {
        log.info("receice job response : {}",docJobResponse);
        //todo
        //将修改数据库中job的状态
        //如果成功更新job状态为成功，更新文档状态为可浏览
        if (docJobResponse.isSuccess()){
            log.info("docjob :{} success,update job status and doc status",docJobResponse.getDocJobId());
            //获取任务的id
            String docJobId = docJobResponse.getDocJobId();
            //通过id获取数据库中的任务
            Optional<DocJobEntity> docJobEntityOptional = docJobService.findById(docJobId);
            if (docJobEntityOptional.isPresent()){
                //如果任务存在，1.修改数据库中job的状态
                //获取job实体
                DocJobEntity docJobEntity = docJobEntityOptional.get();
                //修改job状态
                docJobEntity.setJobStatus("success");
                docJobEntity.setFinishTime(docJobResponse.getFinishTime());
                //保存到数据库
                docJobService.save(docJobEntity);
                //todo
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //2.将文档设置为可预览状态
                // 通过id获取文档对象
                Doc doc = docService.findById(docJobEntity.getDocId()).get();
                //修改文档状态
                doc.setDocStatus("view");
                doc.setNumOfPage(docJobResponse.getNumOfPage());
                //添加文字页码
                //TODO ：将jobType类型保存到doc对象的数据库。没有效果
                log.info("doc num of page: {}",docJobResponse.getNumOfPage());
                doc.setDocType(docJobEntity.getJobType());
                //添加job类型
                log.info("doc job type: {}",docJobEntity.getJobType());
                //保存到数据库
                docService.save(doc);
                log.info("doc content: {}",doc);
                log.info("doc : {},docName:{} can be view",doc.getId(),doc.getDocName());
            }else {
                //如果数据库中任务不存在
                log.error("docjob : {} is not present",docJobResponse.getDocJobId());
                throw new RuntimeException("docjob : "+ docJobResponse.getDocJobId()+ " is not present");
            }
        }else{
            //TODO：失败了要重试，还没做。
            //如果失败，更新job状态为失败，增加重试次数，需要定时调度，从数据库中取出失败job，再次提交
            //如果任务执行次数达到两次还没有成功，则放弃该任务，通知文档上传失败

            log.info("docjob : {} failed,start to retry",docJobResponse.getDocJobId());
        }
    }

    @Override
    public long getProtocolVersion(String protocol, long clientVersion) throws IOException {
        return versionID;
    }

    @Override
    public ProtocolSignature getProtocolSignature(String protocol, long clientVersion, int clientMethodsHash) throws IOException {
        return null;
    }
}
