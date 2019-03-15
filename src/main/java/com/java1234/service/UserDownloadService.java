package com.java1234.service;

import java.util.List;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.java1234.entity.UserDownload;

/**
 * 用户下载Service接口
 * @author Administrator
 *
 */
@Service("userDownloadService")
public interface UserDownloadService {

	/**
	 * 查询某个用户下载某个资源的次数
	 * @param userId
	 * @return
	 */
	public Integer getCountByUserIdAndArticleId(Integer userId,Integer articleId);
	
	/**
	 * 根据条件分页查询用户下载信息
	 * @param s_userDownload
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<UserDownload> list(UserDownload s_userDownload,Integer page,Integer pageSize,Direction direction,String... properties);
	
	/**
	 * 根据条件获取总记录数
	 * @param s_userDownload
	 * @return
	 */
	public Long getCount(UserDownload s_userDownload);
	
	/**
	 * 添加或者修改用户下载信息
	 * @param userDownload
	 */
	public void save(UserDownload userDownload);
	
	/**
	 * 删除该帖子的所有下载信息
	 * @param articleId
	 */
	public void deleteByArticleId(Integer articleId);
	
}
