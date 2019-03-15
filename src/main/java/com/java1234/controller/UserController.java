package com.java1234.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.java1234.entity.User;
import com.java1234.entity.VaptchaMessage;
import com.java1234.service.MessageService;
import com.java1234.service.UserService;
import com.java1234.util.CryptographyUtil;
import com.java1234.util.DateUtil;
import com.java1234.util.StringUtil;

/**
 * 用户控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/user")
public class UserController {

	@Resource
	private UserService userService;
	
	@Resource
	private MessageService messageService;
	
	@Resource
	private JavaMailSender mailSender;
	
	@Value("${userImageFilePath}")
	private String userImageFilePath;
	
	/**
	 * 用户注册
	 * @param user
	 * @param bindingResult
	 * @param session
	 * @return
	 */
	@ResponseBody
	@PostMapping("/register")
	public Map<String,Object> register(@Valid User user,BindingResult bindingResult,String vaptcha_token,HttpServletRequest request)throws Exception{
		Map<String,Object> map=new HashMap<String,Object>();
		if(bindingResult.hasErrors()){
			map.put("success", false);
			map.put("errorInfo", bindingResult.getFieldError().getDefaultMessage());
		}else if(userService.findByUserName(user.getUserName())!=null){
			map.put("success", false);
			map.put("errorInfo", "用户名已存在，请更换！");
		}else if(userService.findByEmail(user.getEmail())!=null){
			map.put("success", false);
			map.put("errorInfo", "邮箱已存在，请更换！");
		}else if(!vaptchaCheck(vaptcha_token,request.getRemoteHost())){
			map.put("success", false);
    		map.put("errorInfo", "人机验证失败！");
		}else{
			user.setPassword(CryptographyUtil.md5(user.getPassword(), CryptographyUtil.SALT)); // md5加密
			user.setRegisterDate(new Date());
			user.setImageName("default.jpg");
			userService.save(user);
			map.put("success", true);
		}
		return map;
	}
	
	/**
	 * 修改密码
	 * @param oldpassword
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
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
	 * 用户登录请求
	 * @param user
	 * @param session
	 * @return
	 */
	@ResponseBody
	@PostMapping("/login")
	public Map<String,Object> login(User user,String vaptcha_token,HttpSession session,HttpServletResponse response,HttpServletRequest request)throws Exception{
		Map<String,Object> map=new HashMap<String,Object>();
		if(StringUtil.isEmpty(user.getUserName())){
			map.put("success", false);
    		map.put("errorInfo", "请输入用户名！");
		}else if(StringUtil.isEmpty(user.getPassword())){
			map.put("success", false);
    		map.put("errorInfo", "请输入密码！");
		}else{
			Subject subject=SecurityUtils.getSubject();
			UsernamePasswordToken token=new UsernamePasswordToken(user.getUserName(), CryptographyUtil.md5(user.getPassword(), CryptographyUtil.SALT));
			try{
				subject.login(token); // 登录验证
				// 人机验证
				if(!vaptchaCheck(vaptcha_token,request.getRemoteHost())){
					map.put("success", false);
		    		map.put("errorInfo", "人机验证失败！");
		    		subject.logout();
				}else{
					String userName=(String) SecurityUtils.getSubject().getPrincipal();
					User currentUser=userService.findByUserName(userName);
					if(currentUser.isOff()){
						map.put("success", false);
						map.put("errorInfo", "该用户已经被封禁，请联系管理员！");
						subject.logout();
					}else{
						currentUser.setMessageCount(messageService.getCountByUserId(currentUser.getId()));
						session.setAttribute("currentUser", currentUser);
						if("管理员".equals(currentUser.getRoleName())){
							session.setMaxInactiveInterval(60*60*6);
						}  
						map.put("success", true);
						
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				map.put("success", false);
				map.put("errorInfo", "用户名或者密码错误！");
			}
		}
		return map;
	}
	
	

	
	/**
	 * 获取当前登录用户是否是vip用户
	 * @param session
	 * @return
	 */
	@ResponseBody
	@PostMapping("/isVip")
	public boolean isVip(HttpSession session){
		User user=(User)session.getAttribute("currentUser");
		return user.isVip();
	}
	
	/**
	 * 上传头像
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/uploadImage")
	public Map<String,Object> uploadImage(MultipartFile file,HttpSession session)throws Exception{
		Map<String,Object> map=new HashMap<String,Object>();
		if(!file.isEmpty()){
			// 获取文件名
			String fileName = file.getOriginalFilename();
			// 获取文件的后缀名
			String suffixName = fileName.substring(fileName.lastIndexOf("."));
			String newFileName=DateUtil.getCurrentDateStr()+suffixName;
			FileUtils.copyInputStreamToFile(file.getInputStream(), new File(userImageFilePath+newFileName));
			map.put("code", 0);
			map.put("msg", "上传成功");
			Map<String,Object> map2=new HashMap<String,Object>();
			map2.put("title", newFileName);
			map2.put("src", "/image/"+DateUtil.getCurrentDatePath()+newFileName);
			map.put("data", map2);
			
			User user=(User)session.getAttribute("currentUser"); 
			user.setImageName(newFileName);
			userService.save(user);
			session.setAttribute("currentUser", user);
		}
		
		return map;
	}
	
	/**
	 * 发送邮件
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/sendEmail")
	public Map<String,Object> sendEmail(String email,HttpSession session)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		if(StringUtil.isEmpty(email)){ // 判断email是否空
			resultMap.put("success", false);
			resultMap.put("errorInfo", "邮件不能为空！");
			return resultMap;
		}
		// 验证email是否存在
		User u=userService.findByEmail(email);
		if(u==null){
			resultMap.put("success", false);
			resultMap.put("errorInfo", "邮件不存在！");
			return resultMap;
		}
		String mailCode=StringUtil.genSixRandomNum();
		// 发邮件
		SimpleMailMessage message = new SimpleMailMessage();//消息构造器
        message.setFrom("1002222344@qq.com");//发件人
        message.setTo(email);//收件人
        message.setSubject("java1234下载站点-用户找回密码");//主题
        message.setText("验证码："+mailCode);//正文
        mailSender.send(message);
        
        // 验证码存session中
        session.setAttribute("mailCode", mailCode);
        session.setAttribute("userId", u.getId());
        resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 邮件验证码判断
	 * @param yzm
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/checkYzm")
	public Map<String,Object> checkYzm(String yzm,HttpSession session)throws Exception{
		Map<String, Object> resultMap = new HashMap<>();
		String mailCode=(String) session.getAttribute("mailCode");
		Integer userId=(Integer) session.getAttribute("userId");
		if(StringUtil.isEmpty(yzm)){
			resultMap.put("success", false);
			resultMap.put("errorInfo", "验证码不能为空！");
			return resultMap;
		}
		if(!yzm.equals(mailCode)){
			resultMap.put("success", false);
			resultMap.put("errorInfo", "验证码错误！");
			return resultMap;
		}
		User user=userService.getById(userId);
		user.setPassword(CryptographyUtil.md5("123456", CryptographyUtil.SALT));
		userService.save(user);
		resultMap.put("success", true);
		return resultMap;
	}
	
	/**
	 * 人机验证结果判断
	 * @param token
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	private boolean vaptchaCheck(String token,String ip)throws Exception{
		String body="";
		CloseableHttpClient httpClient=HttpClients.createDefault();
		HttpPost httpPost=new HttpPost("http://api.vaptcha.com/v2/validate");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("id", "5b53cdb6fc650e53f84fd4c8"));
		nvps.add(new BasicNameValuePair("secretkey", "c2ab71f50ec248a2a5b7bd04cfdf90bc"));
		nvps.add(new BasicNameValuePair("scene", ""));
		nvps.add(new BasicNameValuePair("token", token));
		nvps.add(new BasicNameValuePair("ip", ip));
		
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		
		CloseableHttpResponse r = httpClient.execute(httpPost);
		HttpEntity entity = r.getEntity();
		
		if(entity!=null){
			body = EntityUtils.toString(entity, "utf-8");
			System.out.println(body);
		}
		r.close();
		httpClient.close();
		Gson gson = new Gson();
		VaptchaMessage message=gson.fromJson(body, VaptchaMessage.class);
		if(message.getSuccess()==1){
			return true;
		}else{
			return false;
		}
				
	}
	
	
}
