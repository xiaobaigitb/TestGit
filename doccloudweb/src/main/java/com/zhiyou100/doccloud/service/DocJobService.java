package com.zhiyou100.doccloud.service;

import com.zhiyou100.doccloud.dao.DocJobRepository;
import com.zhiyou100.doccloud.entity.DocJobEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 文档任务的业务层
 */
@Service
public class DocJobService {
    @Autowired
    private DocJobRepository docJobRepository;
    //通过id查找文档任务
    public Optional<DocJobEntity> findById(String id){
        return docJobRepository.findById(id);
    }
    //保存文档任务
    public DocJobEntity save(DocJobEntity docJobEntity){
        return docJobRepository.save(docJobEntity);
    }

}
