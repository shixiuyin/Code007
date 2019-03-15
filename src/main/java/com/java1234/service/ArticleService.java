package com.java1234.service;

import java.util.List;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.java1234.entity.Article;

/**
 * 资源Service接口
 * @author Administrator
 *
 */
@Service("articleService")
public interface ArticleService {
	
	
	
	/**
	 * 根据条件分页查询资源信息
	 * @param s_resource
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<Article> list(Article s_article,Integer page,Integer pageSize,Direction direction,String... properties);
	
	/**
	 * 查询所有资源信息
	 * @return
	 */
	public List<Article> listAll();
	
	/**
	 * 根据条件获取总记录数
	 * @param s_article
	 * @return
	 */
	public Long getCount(Article s_article);
	
	
	/**
	 * 添加或者修改帖子
	 * @param article
	 */
	public void save(Article article);
	
	/**
	 * 删除帖子
	 * @param id
	 */
	public void delete(Integer id);
	
	/**
	 * 根据id获取实体
	 * @param id
	 */
	public Article get(Integer id);
	
}
