package com.java1234.service;

import java.util.List;

import org.springframework.data.domain.Sort.Direction;

import com.java1234.entity.ArcType;

/**
 * 资源类别Service接口
 * @author Administrator
 *
 */
public interface ArcTypeService {

	/**
	 * 分页查询资源类别
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<ArcType> list(Integer page,Integer pageSize,Direction direction, String... properties);
	
	/**
	 * 查询所有资源类别
	 * @return
	 */
	public List<ArcType> listAll(Direction direction, String... properties);
	
	/**
	 * 获取总记录数
	 * @return
	 */
	public Long getCount();
	
	/**
	 * 添加或者修改资源类别
	 * @param resourceType
	 */
	public void save(ArcType resourceType);
	
	/**
	 * 根据id删除资源类别
	 * @param id
	 */
	public void delete(Integer id);
	
	/**
	 * 根据id查找实体
	 * @param id
	 * @return
	 */
	public ArcType getById(Integer id);
	
}
