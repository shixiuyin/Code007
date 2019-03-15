package com.java1234.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.java1234.entity.Comment;
import com.java1234.repository.CommentRepository;
import com.java1234.service.CommentService;

/**
 * 评论Service接口实现类
 * @author Administrator
 *
 */
@Service("commentService")
@Transactional
public class CommentServiceImpl implements CommentService{

	@Resource
	private CommentRepository commentRepository;
	
	@Override
	public List<Comment> list(Comment s_comment, Integer page, Integer pageSize,Direction direction, String... properties) {
		Pageable pageable=new PageRequest(page-1, pageSize, direction,properties);
		Page<Comment> pageComment=commentRepository.findAll(new Specification<Comment>() {
			
			@Override
			public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(s_comment!=null){
					if(s_comment.getState()!=null){
						predicate.getExpressions().add(cb.equal(root.get("state"), s_comment.getState()));
					}
					if(s_comment.getArticle()!=null && s_comment.getArticle().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("article").get("id"), s_comment.getArticle().getId()));
					}
					/*if(s_comment.getUser()!=null && s_comment.getUser().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("user").get("id"), s_comment.getUser().getId()));
					}*/
					if(s_comment.getArticle()!=null && s_comment.getArticle().getUser()!=null && s_comment.getArticle().getUser().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("article").get("user").get("id"), s_comment.getArticle().getUser().getId()));
					}
				}
				return predicate;
			}
		}, pageable);
		return pageComment.getContent();
	}

	@Override
	public Long getTotal(Comment s_comment) {
		Long count=commentRepository.count(new Specification<Comment>() {

			@Override
			public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(s_comment!=null){
					if(s_comment.getState()!=null){
						predicate.getExpressions().add(cb.equal(root.get("state"), s_comment.getState()));
					}
					if(s_comment.getArticle()!=null && s_comment.getArticle().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("article").get("id"), s_comment.getArticle().getId()));
					}
					/*if(s_comment.getUser()!=null && s_comment.getUser().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("user").get("id"), s_comment.getUser().getId()));
					}*/
					if(s_comment.getArticle()!=null && s_comment.getArticle().getUser()!=null && s_comment.getArticle().getUser().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("article").get("user").get("id"), s_comment.getArticle().getUser().getId()));
					}
				}
				return predicate;
			}
		});
		return count;
	}

	@Override
	public void save(Comment comment) {
		commentRepository.save(comment);
	}

	@Override
	public void deleteByArticleId(Integer articleId) {
		commentRepository.deleteByArticleId(articleId);
	}

	@Override
	public void delete(Integer id) {
		commentRepository.delete(id);
	}

	@Override
	public Comment get(Integer id) {
		return commentRepository.findOne(id);
	}

}
