package com.java1234.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.java1234.entity.Article;
import com.java1234.service.ArticleService;
import com.java1234.util.PageUtil;
import com.java1234.util.RedisUtil;

/**
 * 首页或者跳转url跳转控制器
 * @author java1234 小锋 老师
 *
 */
@Controller
public class IndexController {

	@Resource
	private ArticleService articleService;
	
	@Resource
	private RedisUtil<Article> redisUtil;
	
	/**
     * 网站根目录请求
     * @return
     */
    @SuppressWarnings("unchecked")
	@RequestMapping("/")
    public ModelAndView root(HttpServletRequest request) {
    	request.getSession().setAttribute("tMenu", "t_0");
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "首页");
    	Article s_article=new Article();
    	s_article.setHot(true);
    	s_article.setState(2);
    	List<Article> indextHotArticleList=null;
    	if(redisUtil.hasKey("indextHotArticleList")){
    		indextHotArticleList=redisUtil.lGet("indextHotArticleList", 0, -1);
    	}else{
    		indextHotArticleList=articleService.list(s_article, 1, 43,Sort.Direction.DESC,"publishDate");
    		redisUtil.lSet("indextHotArticleList", indextHotArticleList,60*60);
    	}
    	mav.addObject("hotArticleList", indextHotArticleList);
    	Article s_article2=new Article();
    	s_article2.setState(2);
    	List<Article> indexArticleList=null;
    	String key="indexArticleList";
    	if(redisUtil.hasKey(key)){
    		indexArticleList=redisUtil.lGet(key, 0, -1);
    	}else{
    		indexArticleList=articleService.list(s_article2, 1, 20,Sort.Direction.DESC,"publishDate");
    		redisUtil.lSet(key, indexArticleList, 60*60);
    	}
    	mav.addObject("articleList",indexArticleList);
    	Long total=articleService.getCount(s_article2);
    	mav.addObject("pageCode",PageUtil.genPagination("/article/list", total, 1, 20,""));
    	mav.setViewName("index");
        return mav;
    }
    
  
    /**
     * 跳转注册页面
     * @return
     */
    @RequestMapping("/toRegisterPage")
    public ModelAndView toRegisterPage(){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "新注册页面");
    	//mav.addObject("mainPage", "page/registerPage");
    	//mav.addObject("mainPageKey", "#f");
    	mav.setViewName("register");
        return mav;
    }
    
    /**
     * 跳转vip购买页面
     * @return
     */
    @RequestMapping("/toVipPage")
    public ModelAndView toVipPage(){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "购买vip");
    	mav.setViewName("vip");
        return mav;
    }
    
    /**
     * 跳转免责申明页面
     * @return
     */
    @RequestMapping("/toDeclarePage")
    public ModelAndView toDeclarePage(){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "免责申明");
    	mav.setViewName("declare");
        return mav;
    }
}
