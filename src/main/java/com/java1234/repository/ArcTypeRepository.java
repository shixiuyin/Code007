package com.java1234.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.java1234.entity.ArcType;

/**
 * 资源类别Repository接口
 * @author Administrator
 *
 */
public interface ArcTypeRepository extends JpaRepository<ArcType, Integer>,JpaSpecificationExecutor<ArcType> {

}
