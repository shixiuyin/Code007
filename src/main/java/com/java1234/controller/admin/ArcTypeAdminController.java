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

import com.java1234.entity.ArcType;
import com.java1234.init.InitSystem;
import com.java1234.service.ArcTypeService;

/**
 * 管理员-资源类别控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/admin/arcType")
public class ArcTypeAdminController {

	@Resource
	private ArcTypeService arcTypeService;
	
	@Resource
	private InitSystem initSystem;
	
	/**
	 * 根据条件分页查询资源类别信息
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/list")
	@RequiresPermissions(value={"分页查询资源类别信息"})
	public Map<String,Object> list(@RequestParam(value="page",required=false)Integer page,@RequestParam(value="limit",required=false)Integer limit)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		List<ArcType> arcTypeList=arcTypeService.list(page, limit, Sort.Direction.ASC, "sort");
		Long count=arcTypeService.getCount();
		resultMap.put("code", 0);
		resultMap.put("count", count);
		resultMap.put("data", arcTypeList);
		return resultMap;
	}
	
	/**
	 * 添加或者修改类别信息
	 * @param id
	 * @param state
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions(value={"添加或者修改类别信息"})
	public Map<String,Object> save(ArcType arcType,HttpServletRequest request){
		arcTypeService.save(arcType);
		initSystem.loadData(request.getServletContext());
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 删除类别信息
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions(value={"删除类别信息"})
	public Map<String,Object> delete(Integer id,HttpServletRequest request)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		arcTypeService.delete(id);
		initSystem.loadData(request.getServletContext());
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 根据id查询资源类别实体
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/findById")
	@RequiresPermissions(value={"根据id查询资源类别实体"})
	public Map<String,Object> findById(Integer id)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		ArcType arcType=arcTypeService.getById(id);
		resultMap.put("arcType", arcType);
		resultMap.put("success", true);
		return resultMap;
	}
}
