package com.java1234.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java1234.entity.Link;

/**
 * 友情链接Repository接口
 * @author Administrator
 *
 */
public interface LinkRepository extends JpaRepository<Link, Integer>{

}
