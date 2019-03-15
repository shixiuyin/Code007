package com.java1234.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.java1234.entity.Comment;
import com.java1234.service.CommentService;

/**
 * 管理员-评论控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/admin/comment")
public class CommentAdminController {

	@Resource
	private CommentService commentService;
	
	/**
	 * 根据条件分页查询评论信息
	 * @param s_comment
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/list")
	@RequiresPermissions(value={"分页查询评论信息"})
	public Map<String,Object> list(Comment s_comment,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="limit",required=false)Integer limit)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		List<Comment> commentList=commentService.list(s_comment, page, limit, Sort.Direction.DESC, "commentDate");
		Long count=commentService.getTotal(s_comment);
		resultMap.put("code", 0);
		resultMap.put("count", count);
		resultMap.put("data", commentList);
		return resultMap;
	}
	
	/**
	 * 修改评论状态
	 * @param id
	 * @param state
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/updateState")
	@RequiresPermissions(value={"修改评论状态"})
	public Map<String,Object> updateState(Integer id,boolean state){
		Comment comment=commentService.get(id);
		if(state){
			comment.setState(1);			
		}else{
			comment.setState(2);			
		}
		commentService.save(comment);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 删除评论
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions(value={"删除评论"})
	public Map<String,Object> delete(Integer id)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		commentService.delete(id);
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 多选删除
	 * @param ids
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/deleteSelected")
	@RequiresPermissions(value={"删除评论"})
	public Map<String,Object> deleteSelected(String ids)throws Exception{
		String []idsStr=ids.split(",");
		for(int i=0;i<idsStr.length;i++){
			commentService.delete(Integer.parseInt(idsStr[i]));
		}
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
}
