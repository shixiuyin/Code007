package com.java1234.controller.user;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.java1234.entity.Article;
import com.java1234.entity.User;
import com.java1234.service.ArticleService;

/**
 * 用户页面跳转控制器
 * @author Administrator
 *
 */
@Controller
public class IndexUserController {
	
	@Resource
	private ArticleService articleService;
	
	/**
     * 跳转用户中心页面
     * @return
     */
    @RequestMapping("/toUserCenterPage")
    public ModelAndView toUserCenterPage(HttpSession session){
    	User user=(User)session.getAttribute("currentUser");
    	Article s_article=new Article();
    	s_article.setUseful(false);
    	s_article.setUser(user);
    	session.setAttribute("unUsefulArticleCount", articleService.getCount(s_article));
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "用户中心页面");
    	mav.setViewName("user/userCenter");
        return mav;
    }
    
    
}
