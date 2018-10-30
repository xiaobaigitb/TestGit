package com.zhiyou100.doccloud.doccloudweb.dao;

import com.zhiyou100.doccloud.doccloudweb.entity.Doc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
//Doc:表示定义的实体类，Integer：表示主键类型
public interface DocRepository extends JpaRepository<Doc,Integer> {
    //利用反射机制自动识别
    Optional<Doc> findByMd5(String md5);
}
