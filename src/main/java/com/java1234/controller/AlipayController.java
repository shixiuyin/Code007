package com.java1234.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.java1234.entity.User;
import com.java1234.properties.AlipayProperties;
import com.java1234.service.UserService;
import com.java1234.util.DateUtil;
import com.java1234.util.DeviceUtil;
import com.java1234.util.StringUtil;

/**
 * 支付宝支付Controller
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/alipay")
public class AlipayController {

	private static Logger logger=Logger.getLogger(AlipayController.class);
	
	@Resource
	private AlipayProperties alipayProperties;
	
	@Value("${vipMoney}")
	private String vipMoney;
	
	@Resource
	private UserService userService;
	
	/**
	 * 支付请求
	 * @param request
	 * @param response
	 * @param session
	 * @throws Exception
	 */
	@RequestMapping("/pay")
	public void pay(HttpServletRequest request,HttpServletResponse response,HttpSession session)throws Exception{
		User user=(User)session.getAttribute("currentUser");
		String userName=user.getUserName(); // 获取用户名
		String orderNo=userName+"-"+DateUtil.getCurrentDateStr(); // 生成订单号  用户名+日期格式
		
		String form=""; // 生成支付表单
		String userAgent = request.getHeader("user-agent");
		// 封装请求客户端
		AlipayClient client=new DefaultAlipayClient(alipayProperties.getUrl(), alipayProperties.getAppid(), alipayProperties.getRsa_private_key(), alipayProperties.getFormat(), alipayProperties.getCharset(), alipayProperties.getAlipay_public_key(), alipayProperties.getSigntype());
		
		if(DeviceUtil.isMobileDevice(userAgent)){ // 移动设备
			AlipayTradeWapPayRequest alipayRequest=new AlipayTradeWapPayRequest();
			alipayRequest.setReturnUrl(alipayProperties.getReturn_url());
			alipayRequest.setNotifyUrl(alipayProperties.getNotify_url());
			AlipayTradeWapPayModel model=new AlipayTradeWapPayModel();
			model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 设置销售产品码
			model.setOutTradeNo(orderNo); // 设置订单号
			model.setSubject("购买Java1234至尊vip"); // 订单名称
			model.setTotalAmount(vipMoney); // 支付总金额
			model.setBody("Java1234至尊vip 你值得拥有！"); // 设置商品描述
			alipayRequest.setBizModel(model);
			form=client.pageExecute(alipayRequest).getBody(); // 生成表单
		}else{
			// 支付请求
			AlipayTradePagePayRequest alipayRequest=new AlipayTradePagePayRequest();
			alipayRequest.setReturnUrl(alipayProperties.getReturn_url());
			alipayRequest.setNotifyUrl(alipayProperties.getNotify_url());
			AlipayTradePayModel model=new AlipayTradePayModel();
			model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 设置销售产品码
			model.setOutTradeNo(orderNo); // 设置订单号
			model.setSubject("购买Java1234至尊vip"); // 订单名称
			model.setTotalAmount(vipMoney); // 支付总金额
			model.setBody("Java1234至尊vip 你值得拥有！"); // 设置商品描述
			alipayRequest.setBizModel(model);
			
			form=client.pageExecute(alipayRequest).getBody(); // 生成表单
		}
		
		response.setContentType("text/html;charset=" + alipayProperties.getCharset()); 
		response.getWriter().write(form); // 直接将完整的表单html输出到页面 
		response.getWriter().flush(); 
		response.getWriter().close();
	}
	
	/**
	 * 支付宝服务器同步通知页面
	 * @param request
	 * @return
	 */
	@RequestMapping("/returnUrl")
	public ModelAndView returnUrl(HttpServletRequest request)throws Exception{
		ModelAndView mav=new ModelAndView();
		mav.addObject("title", "支付宝支付同步通知地址");
		//获取支付宝GET过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		
		boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipay_public_key(), alipayProperties.getCharset(), alipayProperties.getSigntype()); //调用SDK验证签名

		//——请在这里编写您的程序（以下代码仅作参考）——
		if(signVerified) {
			mav.addObject("message", "充值VIP成功，请重新登录生效，然后联系锋哥(QQ:1002222344)，拉你进VIP群，以及发放Java1234VIP教程！");
		}else {
			mav.addObject("message", "验签失败，请联系管理员(QQ:1002222344)");
		}
		mav.setViewName("returnUrl");
		return mav;
	} 
	
	/**
	 * 支付宝服务器异步通知
	 * @param request
	 * @return
	 */
	@RequestMapping("/notifyUrl")
	public void notifyUrl(HttpServletRequest request)throws Exception{
		logger.info("notifyUrl");
		//获取支付宝GET过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
			System.out.println("name:"+name+",valueStr:"+valueStr);
		}
		
		boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipay_public_key(), alipayProperties.getCharset(), alipayProperties.getSigntype()); //调用SDK验证签名
		//商户订单号
		String out_trade_no = request.getParameter("out_trade_no");
		//交易状态
		String trade_status = request.getParameter("trade_status");
		
		if(signVerified){ // 验证成功 更新订单信息
			if(trade_status.equals("TRADE_FINISHED")){
				logger.info("TRADE_FINISHED");
			}
			if(trade_status.equals("TRADE_SUCCESS")){
				logger.info("TRADE_SUCCESS");
			}
			if(StringUtil.isNotEmpty(out_trade_no)){
				String userName=out_trade_no.split("-")[0];
				User user=userService.findByUserName(userName);
				user.setVip(true);
				user.setPoints(1000000);
				userService.save(user);
			}
		}else{
			logger.error("验证未通过");
		}
				
	} 
}
