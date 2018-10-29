package com.zhiyou100.doccloudweb.service;

import com.zhiyou100.doccloudweb.dao.DocRepository;
import com.zhiyou100.doccloudweb.entity.Doc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocService {

    @Autowired
    private DocRepository docRepository;

    public Optional<Doc> findById(int id) {
        return docRepository.findById(id);
    }
}
