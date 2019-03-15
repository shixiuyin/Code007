package com.java1234.controller.user;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.java1234.entity.User;
import com.java1234.entity.UserDownload;
import com.java1234.service.UserDownloadService;
import com.java1234.util.PageUtil;

/**
 * 用户-用户下载控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/user/userDownload")
public class UserDownloadUserController {

	@Resource
	private UserDownloadService userDownloadService;
	
	
	
	/**
	 * 判断资源是否下载过
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/exist")
	public boolean exist(Integer id,HttpSession session)throws Exception{
		User user=(User)session.getAttribute("currentUser");
		Integer count=userDownloadService.getCountByUserIdAndArticleId(user.getId(), id);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 判断用户积分是否足够下载这个资源
	 * @param id
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/enough")
	public boolean enough(Integer points,HttpSession session)throws Exception{
		User user=(User)session.getAttribute("currentUser");
		if(user.getPoints()>=points){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 *  分页查询用户资源下载信息
	 * @param session
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/list/{id}")
	public ModelAndView list(HttpSession session,@PathVariable(value="id",required=false) Integer page)throws Exception{
		User user=(User)session.getAttribute("currentUser");
		ModelAndView mav=new ModelAndView();
		UserDownload s_userDownload=new UserDownload();
		s_userDownload.setUser(user);
		List<UserDownload> userDownLoadList=userDownloadService.list(s_userDownload, page, 10, Sort.Direction.DESC, "downloadDate");
		mav.addObject("userDownLoadList", userDownLoadList);
		Long total=userDownloadService.getCount(s_userDownload);
		mav.addObject("pageCode", PageUtil.genPagination("/user/userDownload/list", total, page, 10,""));
		mav.addObject("title", "用户已下载资源页面");
		mav.setViewName("user/listUserDownload");
        return mav;
	}
	
}
