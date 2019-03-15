package com.java1234.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.java1234.entity.Link;
import com.java1234.init.InitSystem;
import com.java1234.service.LinkService;

/**
 * 管理员-友情链接控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/admin/link")
public class LinkAdminController {

	@Resource
	private LinkService linkService;
	
	@Resource
	private InitSystem initSystem;
	
	/**
	 * 根据条件分页查询友情链接
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/list")
	@RequiresPermissions(value={"分页查询友情链接"})
	public Map<String,Object> list(@RequestParam(value="page",required=false)Integer page,@RequestParam(value="limit",required=false)Integer limit)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		List<Link> linkList=linkService.list(page, limit, Sort.Direction.ASC, "sort");
		Long count=linkService.getCount();
		resultMap.put("code", 0);
		resultMap.put("count", count);
		resultMap.put("data", linkList);
		return resultMap;
	}
	
	/**
	 * 添加或者修改友情链接
	 * @param link
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions(value={"添加或者修改友情链接"})
	public Map<String,Object> save(Link link,HttpServletRequest request){
		linkService.save(link);
		initSystem.loadData(request.getServletContext());
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 删除友情链接
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions(value={"删除友情链接"})
	public Map<String,Object> delete(Integer id,HttpServletRequest request)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		linkService.delete(id);
		initSystem.loadData(request.getServletContext());
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 根据id查询友情链接实体
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/findById")
	@RequiresPermissions(value={"根据id查询友情链接实体"})
	public Map<String,Object> findById(Integer id)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		Link link=linkService.getById(id);
		resultMap.put("link", link);
		resultMap.put("success", true);
		return resultMap;
	}
}
