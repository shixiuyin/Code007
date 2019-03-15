package com.java1234.service;

import java.util.List;

import org.springframework.data.domain.Sort.Direction;

import com.java1234.entity.Comment;

/**
 * 评论Service接口
 * @author Administrator
 *
 */
public interface CommentService {

	/**
	 * 根据条件分页查询评论信息
	 * @param s_comment
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<Comment> list(Comment s_comment,Integer page,Integer pageSize,Direction direction, String... properties);
	
	/**
	 * 根据条件获取总记录数
	 * @param s_comment
	 * @return
	 */
	public Long getTotal(Comment s_comment);
	
	/**
	 * 保存评论
	 * @param comment
	 */
	public void save(Comment comment);
	
	/**
	 * 删除指定帖子的所有评论
	 * @param articleId
	 */
	public void deleteByArticleId(Integer articleId);
	
	/**
	 * 删除评论
	 * @param id
	 */
	public void delete(Integer id);
	
	/**
	 * 根据id获取实体
	 * @param id
	 * @return
	 */
	public Comment get(Integer id);
}
