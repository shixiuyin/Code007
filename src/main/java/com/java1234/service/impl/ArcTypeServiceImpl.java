package com.java1234.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.java1234.entity.ArcType;
import com.java1234.repository.ArcTypeRepository;
import com.java1234.service.ArcTypeService;

/**
 * 资源类别Service实现类
 * @author Administrator
 *
 */
@Service("arcTypeService")
public class ArcTypeServiceImpl implements ArcTypeService{

	@Resource
	private ArcTypeRepository arcTypeRepository;
	
	@Override
	public List<ArcType> list(Integer page, Integer pageSize,Direction direction, String... properties) {
		Pageable pageable=new PageRequest(page-1, pageSize, direction,properties);
		Page<ArcType> pageArcType=arcTypeRepository.findAll(pageable);
		return pageArcType.getContent();
	}

	@Override
	public List<ArcType> listAll(Direction direction, String... properties) {
		Sort sort=new Sort(direction, properties);
		return arcTypeRepository.findAll(sort);
	}

	@Override
	public Long getCount() {
		return arcTypeRepository.count();
	}

	@Override
	public void save(ArcType arcType) {
		arcTypeRepository.save(arcType);
	}

	@Override
	public void delete(Integer id) {
		arcTypeRepository.delete(id);
	}

	@Override
	public ArcType getById(Integer id) {
		return arcTypeRepository.findOne(id);
	}

}
