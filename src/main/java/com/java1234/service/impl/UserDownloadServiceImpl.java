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

import com.java1234.entity.UserDownload;
import com.java1234.repository.UserDownloadRepository;
import com.java1234.service.UserDownloadService;

/**
 * 用户下载Service实现类
 * @author Administrator
 *
 */
@Service("userDownloadService")
@Transactional
public class UserDownloadServiceImpl implements UserDownloadService{

	@Resource
	private UserDownloadRepository userDownloadRepository;
	
	@Override
	public Integer getCountByUserIdAndArticleId(Integer userId, Integer articleId) {
		return userDownloadRepository.getCountByUserIdAndArticleId(userId, articleId);
	}

	@Override
	public List<UserDownload> list(UserDownload s_userDownload, Integer page, Integer pageSize, Direction direction,
			String... properties) {
		Pageable pageable=new PageRequest(page-1, pageSize, direction,properties);
		Page<UserDownload> pageUserDownload=userDownloadRepository.findAll(new Specification<UserDownload>() {
			
			@Override
			public Predicate toPredicate(Root<UserDownload> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(s_userDownload!=null){
					if(s_userDownload.getUser()!=null && s_userDownload.getUser().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("user").get("id"), s_userDownload.getUser().getId()));
					}
				}
				return predicate;
			}
		},pageable);
		return pageUserDownload.getContent();
	}

	@Override
	public Long getCount(UserDownload s_userDownload) {
		Long count=userDownloadRepository.count(new Specification<UserDownload>() {

			@Override
			public Predicate toPredicate(Root<UserDownload> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(s_userDownload!=null){
					if(s_userDownload.getUser()!=null && s_userDownload.getUser().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("user").get("id"), s_userDownload.getUser().getId()));
					}
				}
				return predicate;
			}
		});
		return count;
	}

	@Override
	public void save(UserDownload userDownload) {
		userDownloadRepository.save(userDownload);
	}

	@Override
	public void deleteByArticleId(Integer articleId) {
		userDownloadRepository.deleteByArticleId(articleId);
	}

}
