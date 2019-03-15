package com.java1234.controller.user;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.java1234.entity.Article;
import com.java1234.entity.User;
import com.java1234.entity.UserDownload;
import com.java1234.lucene.ArticleIndex;
import com.java1234.service.ArticleService;
import com.java1234.service.CommentService;
import com.java1234.service.UserDownloadService;
import com.java1234.service.UserService;
import com.java1234.util.CheckShareLinkEnableUtil;
import com.java1234.util.DateUtil;
import com.java1234.util.RedisUtil;
import com.java1234.util.StringUtil;

/**
 * 用户-帖子控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/user/article")
public class ArticleUserContrller {

	@Resource
	private ArticleService articleService;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private UserDownloadService userDownloadService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private ArticleIndex articleIndex;
	
	@Value("${articleImageFilePath}")
	private String articleImageFilePath;
	
	@Resource
	private RedisUtil<Article> redisUtil;
	
	/**
	 * Layui编辑器图片上传处理
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/uploadImage")
	public Map<String,Object> uploadImage(MultipartFile file)throws Exception{
		Map<String,Object> map=new HashMap<String,Object>();
		if(!file.isEmpty()){
			// 获取文件名
			String fileName = file.getOriginalFilename();
			// 获取文件的后缀名
			String suffixName = fileName.substring(fileName.lastIndexOf("."));
			String newFileName=DateUtil.getCurrentDateStr()+suffixName;
			FileUtils.copyInputStreamToFile(file.getInputStream(), new File(articleImageFilePath+DateUtil.getCurrentDatePath()+newFileName));
			map.put("code", 0);
			map.put("msg", "上传成功");
			Map<String,Object> map2=new HashMap<String,Object>();
			map2.put("title", fileName);
			map2.put("src", "/image/"+DateUtil.getCurrentDatePath()+newFileName);
			map.put("data", map2);
		}
		return map;
	}
	
	/**
	 * 添加帖子
	 * @param article
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/add")
	public ModelAndView add(Article article,HttpSession session)throws Exception{
		User user=(User)session.getAttribute("currentUser");
		article.setPublishDate(new Date());
		article.setUser(user);
		article.setState(1);
		article.setView(StringUtil.randomInteger());
		articleService.save(article);
		ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "发布帖子成功页面");
    	mav.setViewName("user/publishArticleSuccess");
        return mav;
	}
	
	/**
	 * 修改帖子
	 * @param article
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/update")
	public ModelAndView update(Article article)throws Exception{
		Article oldArticle=articleService.get(article.getId());
		oldArticle.setName(article.getName());
		oldArticle.setArcType(article.getArcType());
		oldArticle.setContent(article.getContent());
		oldArticle.setDownload1(article.getDownload1());
		oldArticle.setPassword1(article.getPassword1());
		oldArticle.setPoints(article.getPoints());
		if(oldArticle.getState()==3){ // 假如原先是未通过，用户点击修改，则重新审核 状态编程未审核
			oldArticle.setState(1);
		}
		articleService.save(oldArticle);
		if(oldArticle.getState()==2){
			articleIndex.updateIndex(oldArticle); // 修改索引	
			redisUtil.del("article_"+article.getId());
		}
		ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "修改帖子成功页面");
    	mav.setViewName("user/modifyArticleSuccess");
        return mav;
	}
	
	
	/**
	 * 根据条件分页查询资源帖子信息
	 * @param s_article
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/list")
	public Map<String,Object> list(Article s_article,HttpSession session,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="limit",required=false)Integer limit)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		User user=(User)session.getAttribute("currentUser");
		s_article.setUser(user);
		List<Article> articleList=articleService.list(s_article, page, limit,Sort.Direction.DESC,"publishDate");
		Long count=articleService.getCount(s_article);
		resultMap.put("code", 0);
		resultMap.put("count", count);
		resultMap.put("data", articleList);
		return resultMap;
	}
	
	
	/**
	 * 根据id删除帖子
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/delete")
	public Map<String,Object> delete(Integer id)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		commentService.deleteByArticleId(id); // 删除该帖子下的所有评论
		articleService.delete(id);
		articleIndex.deleteIndex(String.valueOf(id)); // 删除索引
		redisUtil.del("article_"+id);
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 多选删除
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/deleteSelected")
	public Map<String,Object> deleteSelected(String ids)throws Exception{
		String []idsStr=ids.split(",");
		for(int i=0;i<idsStr.length;i++){
			commentService.deleteByArticleId(Integer.parseInt(idsStr[i])); // 删除该帖子下的所有评论
			articleService.delete(Integer.parseInt(idsStr[i]));
			articleIndex.deleteIndex(idsStr[i]); // 删除索引
		}
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
     * 跳转到发布帖子页面
     * @return
     */
    @RequestMapping("/toPublishArticlePage")
    public ModelAndView toPublishArticlePage(){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "发布帖子页面");
    	mav.setViewName("user/publishArticle");
        return mav;
    }
    
    /**
     * 跳转到修改帖子页面
     * @return
     */
    @RequestMapping("/toModifyArticlePage/{id}")
    public ModelAndView toModifyArticlePage(@PathVariable("id") Integer id){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "修改帖子页面");
    	mav.addObject("article", articleService.get(id));
    	mav.setViewName("user/modifyArticle");
        return mav;
    }
    
    /**
     * 跳转到帖子管理页面
     * @return
     */
    @RequestMapping("/toArticleManagePage")
    public ModelAndView toArticleManagePage(){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "帖子管理");
    	mav.setViewName("user/articleManage");
        return mav;
    }
    
    /**
     * 跳转到失效帖子管理页面
     * @return
     */
    @RequestMapping("/toUnUsefulArticleManagePage")
    public ModelAndView toUnUsefulArticleManagePage(){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "失效帖子管理");
    	mav.setViewName("user/unUsefulArticleManage");
        return mav;
    }
    
    
    
    /**
     * 跳转到VIP资源下载页面
     * @return
     */
    @RequestMapping("/toVipDownLoadPage/{id}")
    public ModelAndView toVipDownLoadPage(@PathVariable("id") Integer id,HttpSession session)throws Exception{
    	
    	// 添加记录
    	UserDownload userDownload=new UserDownload();
    	Article article=articleService.get(id);
    	
    	User user=(User)session.getAttribute("currentUser");
    	
    	if(!user.isVip()){ // vip验证
    		return null;
    	}
    	
    	boolean isDownload=false; // 是否下载过
    	Integer count=userDownloadService.getCountByUserIdAndArticleId(user.getId(), id);
		if(count>0){
			isDownload=true;
		}else{
			isDownload=false;
		}
		
		if(!isDownload){
			userDownload.setArticle(article);
			userDownload.setUser(user);
			userDownload.setDownloadDate(new Date());
			userDownloadService.save(userDownload); // 保存用户下载
		}
    	
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("article", articleService.get(id));
    	mav.setViewName("user/downloadPage");
        return mav;
    }
    
    /**
     * 跳转到资源下载页面
     * @return
     */
    @RequestMapping("/toDownLoadPage/{id}")
    public ModelAndView toDownLoadPage(@PathVariable("id") Integer id,HttpSession session)throws Exception{
    	// 添加记录
    	UserDownload userDownload=new UserDownload();
    	Article article=articleService.get(id);
    	
    	User user=(User)session.getAttribute("currentUser");
    	
    	boolean isDownload=false; // 是否下载过
    	Integer count=userDownloadService.getCountByUserIdAndArticleId(user.getId(), id);
		if(count>0){
			isDownload=true;
		}else{
			isDownload=false;
		}
    	
		if(!isDownload){
			if((user.getPoints()-article.getPoints())<0){ // 验证
				return null;
			}	
			// 扣积分
			user.setPoints(user.getPoints()-article.getPoints()); // 扣除积分
			userService.save(user); // 保存用户信息
			
			// 资源分享人家积分
			User articleUser=article.getUser();
			articleUser.setPoints(articleUser.getPoints()+article.getPoints());
			userService.save(articleUser);
			
			userDownload.setArticle(article);
			userDownload.setUser(user);
			userDownload.setDownloadDate(new Date());
			userDownloadService.save(userDownload); // 保存用户下载
		}
    	
	
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("article", articleService.get(id));
    	mav.setViewName("user/downloadPage");
        return mav;
    }
    
    /**
     * 修改百度云分享链接
     * @param article
     * @param session
     * @return
     * @throws Exception
     */
    @ResponseBody
	@RequestMapping("/modifyShareLink")
    public Map<String,Object> modifyShareLink(Article article,HttpSession session)throws Exception{
    	Map<String, Object> resultMap = new HashMap<>();
    	if(CheckShareLinkEnableUtil.check(article.getDownload1())){
    		Article oldArticle=articleService.get(article.getId());
    		oldArticle.setDownload1(article.getDownload1());
    		oldArticle.setPassword1(article.getPassword1());
    		oldArticle.setUseful(true);
    		articleService.save(oldArticle);
    		resultMap.put("success", true);
    		
    		User user=(User)session.getAttribute("currentUser");
        	Article s_article=new Article();
        	s_article.setUseful(false);
        	s_article.setUser(user);
        	session.setAttribute("unUsefulArticleCount", articleService.getCount(s_article));
        	redisUtil.del("article_"+article.getId());
    	}else{
    		resultMap.put("success", false);
    		resultMap.put("errorInfo", "百度云分享链接已经失效 ，请重新发布");
    	}
    	return resultMap;
    }
}
