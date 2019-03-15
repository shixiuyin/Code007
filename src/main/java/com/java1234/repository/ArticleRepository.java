package com.java1234.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.java1234.entity.Article;

/**
 * 资源Repository接口
 * @author Administrator
 *
 */
public interface ArticleRepository extends JpaRepository<Article, Integer>,JpaSpecificationExecutor<Article> {

}
