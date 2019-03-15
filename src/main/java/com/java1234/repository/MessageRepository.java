package com.java1234.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.java1234.entity.Message;

/**
 * 用户消息Repository接口
 * @author Administrator
 *
 */
public interface MessageRepository extends JpaRepository<Message, Integer>,JpaSpecificationExecutor<Message> {

	/**
	 * 查询某个用户下的所有消息
	 * @param userId
	 * @return
	 */
	@Query(value="select count(*) from t_message where is_see=false and user_id=?1",nativeQuery=true)
	public Integer getCountByUserId(Integer userId);
	
	/**
	 * 修改成已经查看状态
	 * @param userId
	 */
	@Query(value="UPDATE t_message SET is_see=TRUE WHERE user_id=?1",nativeQuery=true)
	@Modifying
	public void updateState(Integer userId);
	
}
