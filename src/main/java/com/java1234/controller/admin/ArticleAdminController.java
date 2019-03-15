package com.java1234.controller.admin;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import com.java1234.entity.Message;
import com.java1234.init.InitSystem;
import com.java1234.lucene.ArticleIndex;
import com.java1234.service.ArticleService;
import com.java1234.service.CommentService;
import com.java1234.service.MessageService;
import com.java1234.service.UserDownloadService;
import com.java1234.util.DateUtil;
import com.java1234.util.RedisUtil;

/**
 * 管理员-帖子控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/admin/article")
public class ArticleAdminController {

	@Resource
	private ArticleService articleService;
	
	@Resource
	private MessageService messageService;
	
	@Resource
	private ArticleIndex articleIndex;
	
	@Resource
	private InitSystem initSystem;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private UserDownloadService userDownloadService;
	
	@Value("${articleImageFilePath}")
	private String articleImageFilePath;
	
	@Resource
	private RedisUtil<Article> redisUtil;
	
	/**
	 * 生成所有帖子索引
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/genAllIndex")
	@RequiresPermissions(value={"生成所有帖子索引"})
	public boolean genAllIndex(){
		List<Article> articleList=articleService.listAll();
		for(Article article:articleList){
			try {
				articleIndex.addIndex(article);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
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
	@RequiresPermissions(value={"分页查询资源帖子信息"})
	public Map<String,Object> list(Article s_article,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="limit",required=false)Integer limit)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		List<Article> articleList=articleService.list(s_article, page, limit,Sort.Direction.DESC,"publishDate");
		Long count=articleService.getCount(s_article);
		resultMap.put("code", 0);
		resultMap.put("count", count);
		resultMap.put("data", articleList);
		return resultMap;
	}
	
	/**
     * 跳转到修改帖子页面
     * @return
     */
    @RequestMapping("/toModifyArticlePage/{id}")
    @RequiresPermissions(value={"跳转到修改帖子页面"})
    public ModelAndView toModifyArticlePage(@PathVariable("id") Integer id){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "修改帖子页面");
    	mav.addObject("article", articleService.get(id));
    	mav.setViewName("admin/modifyArticle");
        return mav;
    }
    
    /**
     * 跳转到帖子审核页面
     * @param id
     * @return
     */
    @RequestMapping("/toReViewArticlePage/{id}")
    @RequiresPermissions(value={"跳转到帖子审核页面"})
    public ModelAndView toReViewArticlePage(@PathVariable("id") Integer id){
    	ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "修改帖子页面");
    	mav.addObject("article", articleService.get(id));
    	mav.setViewName("admin/reviewArticle");
        return mav;
    }
    
    /**
	 * Layui编辑器图片上传处理
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/uploadImage")
	@RequiresPermissions(value={"图片上传"})
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
			map2.put("title", newFileName);
			map2.put("src", "/image/"+DateUtil.getCurrentDatePath()+newFileName);
			map.put("data", map2);
		}
		return map;
	}
    
    /**
	 * 修改帖子
	 * @param article
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/update")
	@RequiresPermissions(value={"修改帖子"})
	public ModelAndView update(Article article)throws Exception{
		Article oldArticle=articleService.get(article.getId());
		oldArticle.setName(article.getName());
		oldArticle.setArcType(article.getArcType());
		oldArticle.setContent(article.getContent());
		oldArticle.setDownload1(article.getDownload1());
		oldArticle.setPassword1(article.getPassword1());
		oldArticle.setPoints(article.getPoints());
		if(oldArticle.getState()==2){  // 当审核通过的时候 才更新索引
			articleIndex.updateIndex(oldArticle); // 修改索引			
		}
		articleService.save(oldArticle);
		redisUtil.del("indextHotArticleList","indexArticleList","article_"+article.getId());
		ModelAndView mav=new ModelAndView();
    	mav.addObject("title", "修改帖子成功页面");
    	mav.setViewName("admin/modifyArticleSuccess");
        return mav;
	}
	
	
	/**
	 * 根据id删除帖子
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions(value={"删除帖子"})
	public Map<String,Object> delete(Integer id)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		redisUtil.del("indextHotArticleList","indexArticleList","article_"+id);
		commentService.deleteByArticleId(id); // 删除该帖子下的所有评论
		userDownloadService.deleteByArticleId(id);
		articleService.delete(id);
		articleIndex.deleteIndex(String.valueOf(id)); // 删除索引
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
	@RequiresPermissions(value={"删除帖子"})
	public Map<String,Object> deleteSelected(String ids)throws Exception{
		String []idsStr=ids.split(",");
		for(int i=0;i<idsStr.length;i++){
			String articleId=idsStr[i];
			redisUtil.del("article_"+idsStr[i],"hotArticleList_type_"+articleService.get(Integer.parseInt(articleId)).getArcType().getId());
			commentService.deleteByArticleId(Integer.parseInt(articleId)); // 删除该帖子下的所有评论
			userDownloadService.deleteByArticleId(Integer.parseInt(articleId));
			articleService.delete(Integer.parseInt(articleId));
			articleIndex.deleteIndex(articleId); // 删除索引
		}
		redisUtil.del("indextHotArticleList","indexArticleList");
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 修改状态
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/updateState")
	@RequiresPermissions(value={"修改状态"})
	public Map<String,Object> updateState(Article article)throws Exception{
		Article oldArticle=articleService.get(article.getId());
		Message message=new Message();
		message.setUser(oldArticle.getUser());
		message.setPublishDate(new Date());
		if(article.getState()==2){
			oldArticle.setState(2);
			articleIndex.addIndex(oldArticle); // 添加索引
			message.setContent("【审核成功】 您发布的【"+oldArticle.getName()+"】帖子审核成功！");
		}else if(article.getState()==3){
			oldArticle.setState(3);
			oldArticle.setReason(article.getReason());
			message.setContent("【审核失败】 您发布的【"+oldArticle.getName()+"】帖子审核未成功，原因是："+article.getReason());
		}
		articleService.save(oldArticle);
		messageService.save(message); // 保存用户消息
		redisUtil.del("indextHotArticleList","indexArticleList","article_"+article.getId());
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 修改热门状态
	 * @param article
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/updateHotState")
	@RequiresPermissions(value={"修改热门状态"})
	public Map<String,Object> updateHotState(Article article,HttpServletRequest request)throws Exception{
		Article oldArticle=articleService.get(article.getId());
		oldArticle.setHot(article.isHot());
		articleService.save(oldArticle);
		initSystem.loadData(request.getServletContext()); // 刷新系统缓存
		redisUtil.del("indextHotArticleList","indexArticleList","article_"+article.getId());
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	
}
