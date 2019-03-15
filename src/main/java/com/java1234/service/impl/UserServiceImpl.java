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

import com.java1234.entity.User;
import com.java1234.repository.UserRepository;
import com.java1234.service.UserService;
import com.java1234.util.StringUtil;

/**
 * 用户Service实现类
 * @author Administrator
 *
 */
@Service("userService")
public class UserServiceImpl implements UserService{

	@Resource
	private UserRepository userRepository;
	
	@Override
	public void save(User user) {
		userRepository.save(user);
	}

	@Override
	public User getById(Integer id) {
		return userRepository.findOne(id);
	}

	@Override
	public User findByUserName(String userName) {
		return userRepository.findByUserName(userName);
	}

	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public List<User> list(User s_user, Integer page, Integer pageSize, Direction direction, String... properties) {
		Pageable pageable=new PageRequest(page-1, pageSize, direction,properties);
		Page<User> pageUser=userRepository.findAll(new Specification<User>() {
			
			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(s_user!=null){
					if(StringUtil.isNotEmpty(s_user.getUserName())){
						predicate.getExpressions().add(cb.like(root.get("userName"), "%"+s_user.getUserName()+"%"));
					}
				}
				return predicate;
			}
		},pageable);
		return pageUser.getContent();
	}

	@Override
	public Long getCount(User s_user) {
		Long count=userRepository.count(new Specification<User>() {
			
			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(s_user!=null){
					if(StringUtil.isNotEmpty(s_user.getUserName())){
						predicate.getExpressions().add(cb.like(root.get("userName"), "%"+s_user.getUserName()+"%"));
					}
				}
				return predicate;
			}
		});
		return count;
	}

}
