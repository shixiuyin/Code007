package com.java1234.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.java1234.entity.Article;
import com.java1234.repository.ArticleRepository;
import com.java1234.service.ArticleService;
import com.java1234.util.StringUtil;

/**
 * 资源Service实现类
 * @author Administrator
 *
 */
@Service("articleService")
public class ArticleServiceImpl implements ArticleService{

	@Resource
	private ArticleRepository articleRepository;
	
	@Override
	public List<Article> list(Article s_article, Integer page, Integer pageSize,Direction direction,String... properties) {
		Pageable pageable=new PageRequest(page-1, pageSize, direction,properties);
		Page<Article> pageArticle=articleRepository.findAll(new Specification<Article>() {
			
			@Override
			public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(s_article!=null){
					if(StringUtil.isNotEmpty(s_article.getName())){
						predicate.getExpressions().add(cb.like(root.get("name"), "%"+s_article.getName().trim()+"%"));
					}
					if(s_article.isHot()){
						predicate.getExpressions().add(cb.equal(root.get("isHot"), 1));
					}
					if(s_article.getArcType()!=null && s_article.getArcType().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("arcType").get("id"), s_article.getArcType().getId()));
					}
					if(s_article.getUser()!=null && s_article.getUser().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("user").get("id"), s_article.getUser().getId()));
					}
					if(s_article.getUser()!=null && StringUtil.isNotEmpty(s_article.getUser().getUserName())){
						predicate.getExpressions().add(cb.like(root.get("user").get("userName"), "%"+s_article.getUser().getUserName()+"%"));
					}
					if(s_article.getState()!=null){
						predicate.getExpressions().add(cb.equal(root.get("state"), s_article.getState()));
					}
					if(!s_article.isUseful()){
						predicate.getExpressions().add(cb.equal(root.get("isUseful"), false));
					}
				}
				return predicate;
			}
		}, pageable);
		return pageArticle.getContent();
	}

	@Override
	public Long getCount(Article s_article) {
		Long count=articleRepository.count(new Specification<Article>() {

			@Override
			public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(s_article!=null){
					if(StringUtil.isNotEmpty(s_article.getName())){
						predicate.getExpressions().add(cb.like(root.get("name"), "%"+s_article.getName().trim()+"%"));
					}
					if(s_article.isHot()){
						predicate.getExpressions().add(cb.equal(root.get("isHot"), 1));
					}
					if(s_article.getArcType()!=null && s_article.getArcType().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("arcType").get("id"), s_article.getArcType().getId()));
					}
					if(s_article.getUser()!=null && s_article.getUser().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("user").get("id"), s_article.getUser().getId()));
					}
					if(s_article.getUser()!=null && StringUtil.isNotEmpty(s_article.getUser().getUserName())){
						predicate.getExpressions().add(cb.like(root.get("user").get("userName"),"%"+s_article.getUser().getUserName()+"%"));
					}
					if(s_article.getState()!=null){
						predicate.getExpressions().add(cb.equal(root.get("state"), s_article.getState()));
					}
					if(!s_article.isUseful()){
						predicate.getExpressions().add(cb.equal(root.get("isUseful"), false));
					}
				}
				return predicate;
			}
		});
		return count;
	}


	@Override
	public List<Article> listAll() {
		return articleRepository.findAll();
	}

	@Override
	public void save(Article article) {
		articleRepository.save(article);
	}

	@Override
	public void delete(Integer id) {
		articleRepository.delete(id);
	}

	@Override
	public Article get(Integer id) {
		return articleRepository.findOne(id);
	}

}
