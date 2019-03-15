package com.java1234.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.java1234.entity.ArcType;
import com.java1234.entity.Article;
import com.java1234.entity.Comment;
import com.java1234.init.InitSystem;
import com.java1234.lucene.ArticleIndex;
import com.java1234.service.ArcTypeService;
import com.java1234.service.ArticleService;
import com.java1234.service.CommentService;
import com.java1234.util.PageUtil;
import com.java1234.util.RedisUtil;
import com.java1234.util.StringUtil;

/**
 * 资源帖子控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/article")
public class ArticleController {

	@Resource
	private ArticleService articleService;
	
	@Resource
	private ArcTypeService arcTypeService;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private ArticleIndex articleIndex;
	
	@Resource
	private RedisUtil<Article> redisUtil;
	
	
	/** 
	 * 分页查询资源帖子信息
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/list/{id}")
	public ModelAndView list(@RequestParam(value="typeId",required=false)Integer typeId,@PathVariable(value="id",required=false) Integer page,HttpServletRequest request)throws Exception{
		if(typeId!=null){
			request.getSession().setAttribute("tMenu", "t_"+typeId);
		}
		ModelAndView mav=new ModelAndView();
		Article s_article=new Article();
		s_article.setHot(true);
		s_article.setState(2);
		if(typeId==null){
			mav.addObject("title", "第"+page+"页");
		}else{
			ArcType arcType=InitSystem.arcTypeMap.get(typeId);
			mav.addObject("title", arcType.getName()+"-第"+page+"页");
			s_article.setArcType(arcType);
		}
    	mav.addObject("hotArticleList", articleService.list(s_article, 1, 43,Sort.Direction.DESC,"publishDate"));
    	s_article.setHot(false);
		mav.addObject("articleList", articleService.list(s_article, page, 20,Sort.Direction.DESC,"publishDate"));
		Long total=articleService.getCount(s_article);
		StringBuffer param=new StringBuffer();
		if(typeId!=null){
			param.append("?typeId="+typeId);
		}
		mav.addObject("pageCode",PageUtil.genPagination("/article/list", total, page, 20,param.toString()));
		mav.addObject("mainPage", "page/indexPage");
    	mav.addObject("mainPageKey", "#f");
    	mav.setViewName("index");
		return mav;
	}
	
	/**
	 * 根据id查询帖子详细信息
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/{id}")
	public ModelAndView view(@PathVariable("id") Integer id)throws Exception{
		ModelAndView mav=new ModelAndView();
		Article article=null;
		String key="article_"+id;
		if(redisUtil.hasKey(key)){
			article=(Article) redisUtil.get(key);
		}else{
			article=articleService.get(id);
			redisUtil.set(key, article,60*60);
		}
		mav.addObject("article", article);
		mav.addObject("title", article.getName());
		Article s_article=new Article();
		s_article.setHot(true);
		s_article.setArcType(article.getArcType());
		
		List<Article> hotArticleList=null;
		String hKey="hotArticleList_type_"+article.getArcType().getId();
		if(redisUtil.hasKey(hKey)){
			hotArticleList=redisUtil.lGet(hKey, 0, -1);
		}else{
			hotArticleList=articleService.list(s_article, 1, 43,Sort.Direction.DESC,"publishDate");
			redisUtil.lSet(hKey, hotArticleList, 60*60);
		}
		hotArticleList=articleService.list(s_article, 1, 43,Sort.Direction.DESC,"publishDate");
		mav.addObject("hotArticleList", hotArticleList);
		mav.addObject("mainPage", "page/articlePage");
    	mav.addObject("mainPageKey", "#f");
    	Comment s_comment=new Comment();
    	s_comment.setArticle(article);
    	s_comment.setState(1); // 只统计已经审核通过的
    	mav.addObject("commentCount", commentService.getTotal(s_comment));
    	mav.setViewName("article");
		return mav;
	}
	
	/**
	 * 查看次数加1
	 * @param id
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/updateView")
	public void updateView(Integer id)throws Exception{
		Article article=articleService.get(id);
		article.setView(article.getView()+1);
		articleService.save(article);
	}
	
	/**
	 * 关键字分词搜索
	 * @param q
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/search")
	public ModelAndView search(String q,@RequestParam(value="page",required=false)String page,HttpServletRequest request)throws Exception{
		request.getSession().setAttribute("tMenu", "t_0");
		if(StringUtil.isEmpty(page)){
			page="1";
		}
		ModelAndView mav=new ModelAndView();
		Article s_article=new Article();
    	s_article.setHot(true);
    	mav.addObject("hotArticleList", articleService.list(s_article, 1, 43,Sort.Direction.DESC,"publishDate"));
		List<Article> articleList=articleIndex.search(q);
		Integer toIndex=articleList.size()>=Integer.parseInt(page)*10?Integer.parseInt(page)*10:articleList.size();
		mav.addObject("articleList",articleList.subList((Integer.parseInt(page)-1)*10, toIndex));
		mav.addObject("pageCode", this.genUpAndDownPageCode(Integer.parseInt(page), articleList.size(), q, 10));
		mav.addObject("q",q);
		mav.addObject("resultTotal",articleList.size());
		mav.addObject("title", q);
		mav.addObject("mainPage", "page/resultPage");
    	mav.addObject("mainPageKey", "#f");
		mav.setViewName("result");
		return mav;
	}
	
	/**
	 * 加载相关资源
	 * @param q
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/loadRelatedResource")
	public List<Article> loadRelatedResource(String q)throws Exception{
		if(StringUtil.isEmpty(q)){
			return null;
		}
		List<Article> articleList=articleIndex.searchNoHighLighter(q);
		return articleList.size()>20?articleList.subList(0, 20):articleList;
	}
	
	/**
	 * 生成上一页，下一页代码
	 * @param page
	 * @param totalNum
	 * @param q
	 * @param pageSize
	 * @return
	 */
	private String genUpAndDownPageCode(Integer page,Integer totalNum,String q,Integer pageSize){
		long totalPage=totalNum%pageSize==0?totalNum/pageSize:totalNum/pageSize+1;
		StringBuffer pageCode=new StringBuffer();
		if(totalPage==0){
			return "";
		}else{
			pageCode.append("<div class='layui-box layui-laypage layui-laypage-default'>");
			if(page>1){
				pageCode.append("<a href='/article/search?page="+(page-1)+"&q="+q+"' class='layui-laypage-prev'>上一页</a>");
			}else{
				pageCode.append("<a href='#' class='layui-laypage-prev layui-disabled'>上一页</a>");
			}
			if(page<totalPage){
				pageCode.append("<a href='/article/search?page="+(page+1)+"&q="+q+"' class='layui-laypage-next'>下一页</a>");				
			}else{
				pageCode.append("<a href='#' class='layui-laypage-next layui-disabled'>下一页</a>");
			}
			pageCode.append("</div>");
		}
		return pageCode.toString();
	}
}
