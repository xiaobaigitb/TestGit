package com.zhiyou100.doccloudweb.dao;

import com.zhiyou100.doccloudweb.entity.Doc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
//Doc:表示定义的实体类
public interface DocRepository extends JpaRepository<Doc,Integer> {
}
