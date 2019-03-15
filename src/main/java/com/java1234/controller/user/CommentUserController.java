package com.java1234.controller.user;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.java1234.entity.Article;
import com.java1234.entity.Comment;
import com.java1234.entity.User;
import com.java1234.service.CommentService;

/**
 * 用户-评论控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/user/comment")
public class CommentUserController {

	@Resource
	private CommentService commentService;
	
	/**
	 * 保存评论信息
	 * @param content
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
    @PostMapping("/save")
	public Map<String,Object> save(Comment comment,HttpSession session)throws Exception{
		Map<String,Object> map=new HashMap<String,Object>();
		comment.setCommentDate(new Date());
		comment.setState(0);
		comment.setUser((User)session.getAttribute("currentUser"));
		commentService.save(comment);
		map.put("success", true);
		return map;
	}
	
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
	public Map<String,Object> list(Comment s_comment,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="limit",required=false)Integer limit,HttpSession session)throws Exception{
		User user=(User)session.getAttribute("currentUser");
		Article article=new Article();
		article.setUser(user);
		s_comment.setArticle(article);
		s_comment.setState(1); // 审核通过
		Map<String, Object> resultMap = new HashMap<>();
		List<Comment> commentList=commentService.list(s_comment, page, limit, Sort.Direction.DESC, "commentDate");
		Long count=commentService.getTotal(s_comment);
		resultMap.put("code", 0);
		resultMap.put("count", count);
		resultMap.put("data", commentList);
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
	public Map<String,Object> deleteSelected(String ids)throws Exception{
		String []idsStr=ids.split(",");
		for(int i=0;i<idsStr.length;i++){
			commentService.delete(Integer.parseInt(idsStr[i]));
		}
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
     * 跳转到评论管理页面
     * @return
     */
    @RequestMapping("/toCommentManagePage")
    public ModelAndView toCommentManagePage(){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "评论管理");
    	mav.setViewName("user/commentManage");
        return mav;
    }
}
