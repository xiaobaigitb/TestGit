package com.zhiyou100.doccloud.dao;

import com.zhiyou100.doccloud.entity.DocJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocJobRepository extends JpaRepository<DocJobEntity,String> {

}
