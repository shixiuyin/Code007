package com.java1234.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.java1234.entity.User;
import com.java1234.service.UserService;
import com.java1234.util.CryptographyUtil;

/**
 * 管理员-用户控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/admin/user")
public class UserAdminController {
	
	@Resource
	private UserService userService;
	
	/**
	 * 根据条件分页查询用户信息
	 * @param s_user
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/list")
	@RequiresPermissions(value={"分页查询用户信息"})
	public Map<String,Object> list(User s_user,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="limit",required=false)Integer limit)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		List<User> userList=userService.list(s_user, page, limit, Sort.Direction.DESC, "registerDate");
		Long count=userService.getCount(s_user);
		resultMap.put("code", 0);
		resultMap.put("count", count);
		resultMap.put("data", userList);
		return resultMap;
	}
	
	/**
	 * 修改用户VIP状态
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/updateVipState")
	@RequiresPermissions(value={"修改用户VIP状态"})
	public Map<String,Object> updateVipState(User user)throws Exception{
		User oldUser=userService.getById(user.getId());
		oldUser.setVip(user.isVip());
		userService.save(oldUser);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 修改用户状态
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/updateUserState")
	@RequiresPermissions(value={"修改用户状态"})
	public Map<String,Object> updateUserState(User user)throws Exception{
		User oldUser=userService.getById(user.getId());
		oldUser.setOff(user.isOff());
		userService.save(oldUser);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 重置用户密码
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/resetPassword")
	@RequiresPermissions(value={"重置用户密码"})
	public Map<String,Object> resetPassword(Integer id)throws Exception{
		User oldUser=userService.getById(id);
		oldUser.setPassword(CryptographyUtil.md5("123456", CryptographyUtil.SALT)); // 重置默认密码 123456
		userService.save(oldUser);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 用户积分充值
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/addPoints")
	@RequiresPermissions(value={"用户积分充值"})
	public Map<String,Object> addPoints(User user)throws Exception{
		User oldUser=userService.getById(user.getId());
		oldUser.setPoints(oldUser.getPoints()+user.getPoints());
		userService.save(oldUser);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 修改密码
	 * @param oldpassword
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequiresPermissions(value={"修改管理员密码"})
	@PostMapping("/modifyPassword")
	public Map<String,Object> modifyPassword(String oldpassword,String password,HttpSession session)throws Exception{
		User user=(User)session.getAttribute("currentUser");
		Map<String,Object> map=new HashMap<String,Object>();
		if(!user.getPassword().equals(CryptographyUtil.md5(oldpassword, CryptographyUtil.SALT))){
			map.put("success", false);
			map.put("errorInfo", "原密码错误！");
			return map;
		}
		User oldUser=userService.getById(user.getId());
		oldUser.setPassword(CryptographyUtil.md5(password, CryptographyUtil.SALT));
		userService.save(oldUser);
		map.put("success", true);
		return map;
	}
	
	/**
	 * 安全退出
	 * @return
	 */
	@RequiresPermissions(value={"安全退出"})
	@GetMapping("/logout")
	public String logout(HttpSession session){
		SecurityUtils.getSubject().logout();
		return "redirect:/adminLogin.html";
	}
	
}
