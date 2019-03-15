package com.java1234.controller.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.java1234.entity.Article;
import com.java1234.entity.Message;
import com.java1234.entity.User;
import com.java1234.service.ArticleService;
import com.java1234.service.MessageService;
import com.java1234.util.PageUtil;

/**
 * 用户-消息控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/user/message")
public class MessageUserController {

	@Resource
	private MessageService messageService;
	
	@Resource
	private ArticleService articleService;
	
	/**
	 * 查询用户的总消息记录数
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
    @PostMapping("/getCountByUserId")
	public Map<String,Object> getCountByUserId(Integer userId)throws Exception{
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("messageCount", messageService.getCountByUserId(userId));
		map.put("success", true);
		return map;
	}
	
	/**
	 * 分页查询消息帖子信息
	 * @param session
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/list/{id}")
	public ModelAndView list(HttpSession session,@PathVariable(value="id",required=false) Integer page)throws Exception{
		ModelAndView mav=new ModelAndView();
		Message s_message=new Message();
		User user=(User)session.getAttribute("currentUser");
		s_message.setUser(user);
		List<Message> messageList=messageService.list(s_message, page, 10, Sort.Direction.DESC, "publishDate");
		mav.addObject("messageList", messageList);
		Long total=messageService.getCount(s_message);
		mav.addObject("pageCode", PageUtil.genPagination("/user/message/list", total, page, 10,""));
		mav.addObject("title", "系统消息页面");
		mav.setViewName("user/listMessage");
        return mav;
	}
	
	/**
	 * 查看系统消息 状态都改成已经查看
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/see")
	public ModelAndView See(HttpSession session)throws Exception{
		User user=(User)session.getAttribute("currentUser");
		Article s_article=new Article();
    	s_article.setUseful(false);
    	s_article.setUser(user);
		session.setAttribute("unUsefulArticleCount", articleService.getCount(s_article));
		ModelAndView mav=new ModelAndView();
		messageService.updateState(user.getId());
		user.setMessageCount(0);
		session.setAttribute("currentUser", user);
		Message s_message=new Message();
		s_message.setUser(user);
		List<Message> messageList=messageService.list(s_message, 1, 10, Sort.Direction.DESC, "publishDate");
		mav.addObject("messageList", messageList);
		Long total=messageService.getCount(s_message);
		mav.addObject("pageCode", PageUtil.genPagination("/user/message/list", total, 1, 10,""));
		mav.addObject("title", "系统消息页面");
		mav.setViewName("user/listMessage");
        return mav;
	}
	
}
