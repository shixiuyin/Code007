package com.java1234.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.java1234.entity.User;
import com.java1234.properties.WeixinpayProperties;
import com.java1234.service.UserService;
import com.java1234.util.DateUtil;
import com.java1234.util.DeviceUtil;
import com.java1234.util.HttpClientUtil;
import com.java1234.util.MD5Util;
import com.java1234.util.StringUtil;
import com.java1234.util.XmlUtil;

/**
 * 微信支付Controller
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/weixinpay")
public class WeixinpayController {

	private static Logger logger=Logger.getLogger(WeixinpayController.class);
	
	@Resource
	private WeixinpayProperties weixinpayProperties;
	
	@Value("${vipMoney}")
	private String vipMoney;
	
	@Resource
	private UserService userService;
	
	/**
	 * 支付请求
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws Exception
	 */
	@RequestMapping("/pay")
	public ModelAndView pay(HttpServletRequest request,HttpServletResponse response,HttpSession session) throws ServletException, Exception{
		
		User user=(User)session.getAttribute("currentUser");
		String userName=user.getUserName(); // 获取用户名
		String orderNo=userName+"-"+DateUtil.getCurrentDateStr(); // 生成订单号  用户名+日期格式
		
		String userAgent = request.getHeader("user-agent");
		
		ModelAndView mav=new ModelAndView();
		
		if(DeviceUtil.isMobileDevice(userAgent)){ // 移动设备
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("appid", weixinpayProperties.getAppid());
			map.put("mch_id", weixinpayProperties.getMch_id());
			map.put("device_info", weixinpayProperties.getDevice_info());
			map.put("notify_url", weixinpayProperties.getNotify_url());
			map.put("trade_type", "MWEB"); // 交易类型
			map.put("out_trade_no", orderNo);
			map.put("body", "Java1234至尊vip 你值得拥有！");
			map.put("total_fee", Integer.parseInt(vipMoney)*100);
			map.put("nonce_str", StringUtil.getRandomString(30)); // 随机串
			map.put("spbill_create_ip", getRemortIP(request)); // 终端IP
			map.put("sign", getSign(map)); // 签名
			String xml=XmlUtil.genXml(map);
			System.out.println(xml);
			InputStream in = HttpClientUtil.sendXMLDataByPost(weixinpayProperties.getUrl().toString(),xml).getEntity().getContent(); // 发送xml消息
			String mweb_url=getElementValue(in,"mweb_url");
			logger.info("mweb_url:"+mweb_url);
			// 拼接跳转地址 
			mweb_url+="&redirect_url="+URLEncoder.encode(weixinpayProperties.getReturn_url(),"GBK");
			logger.info("编码：mweb_url:"+mweb_url);
     		response.sendRedirect(mweb_url);
     		return null;
		}else{
			mav.addObject("orderNo", orderNo);
			mav.addObject("title", "充值VIP_Java1234下载");
			mav.setViewName("weixinpay");
			return mav;
		}
	}
	
	/**
	 * 微信支付服务器异步通知
	 * @param request
	 * @throws Exception
	 */
	@RequestMapping("/notifyUrl")
	public void notifyUrl(HttpServletRequest request)throws Exception{
		logger.info("notifyUrl");
		//读取参数    
        InputStream inputStream ;    
        StringBuffer sb = new StringBuffer();    
        inputStream = request.getInputStream();    
        String s ;    
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));    
        while ((s = in.readLine()) != null){    
            sb.append(s);    
        }    
        in.close();    
        inputStream.close();    
    
        //解析xml成map    
        Map<String, String> m = new HashMap<String, String>();    
        m = XmlUtil.doXMLParse(sb.toString());    
            
        //过滤空 设置 TreeMap    
        SortedMap<Object,Object> packageParams = new TreeMap<Object,Object>();          
        Iterator<String> it = m.keySet().iterator();    
        while (it.hasNext()) {    
            String parameter = it.next();    
            String parameterValue = m.get(parameter);    
                
            String v = "";    
            if(null != parameterValue) {    
                v = parameterValue.trim();    
            } 
            logger.info("p:"+parameter+",v:"+v);
            packageParams.put(parameter, v);    
        }  
        
        // 微信支付的API密钥    
        String key = weixinpayProperties.getKey();  
        
        if(isTenpaySign("UTF-8", packageParams, key)){ // 验证通过
        	if("SUCCESS".equals((String)packageParams.get("result_code"))){  
        		String out_trade_no=(String)packageParams.get("out_trade_no");
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
	
	/**
	 * 查询订单支付状态
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/loadPayState")
	public Boolean loadPayState(String orderNo)throws Exception{
		String userName=orderNo.split("-")[0];
		return userService.findByUserName(userName).isVip();
	}
	
	/**
	 * 微信支付同步通知页面
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/returnUrl")
	public ModelAndView returnUrl(HttpServletRequest request)throws Exception{
		ModelAndView mav=new ModelAndView();
		mav.addObject("title", "微信支付同步通知地址");
		mav.addObject("message", "充值VIP成功，请重新登录生效，然后联系锋哥(QQ:1002222344)，拉你进VIP群，以及发放Java1234VIP教程！");
		mav.setViewName("returnUrl");
		return mav;
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/loadPayImage")
	public void loadPayImage(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String orderNo=request.getParameter("orderNo");
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("appid", weixinpayProperties.getAppid());
		map.put("mch_id", weixinpayProperties.getMch_id());
		map.put("device_info", weixinpayProperties.getDevice_info());
		map.put("notify_url", weixinpayProperties.getNotify_url());
		map.put("trade_type", "NATIVE"); // 交易类型
		map.put("out_trade_no", orderNo);
		map.put("body", "Java1234至尊vip 你值得拥有！");
		map.put("total_fee", Integer.parseInt(vipMoney)*100);
		map.put("nonce_str", StringUtil.getRandomString(30)); // 随机串
		map.put("spbill_create_ip", getRemortIP(request)); // 终端IP
		map.put("sign", getSign(map)); // 签名
		String xml=XmlUtil.genXml(map);
		System.out.println(xml);
		
		InputStream in = HttpClientUtil.sendXMLDataByPost(weixinpayProperties.getUrl().toString(),xml).getEntity().getContent(); // 发送xml消息
		String code_url=getElementValue(in,"code_url"); 
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();  
	     Map hints = new HashMap();  
	     BitMatrix bitMatrix = null;  
	     try {  
	         bitMatrix = multiFormatWriter.encode(code_url, BarcodeFormat.QR_CODE, 250, 250,hints);  
	         BufferedImage image = toBufferedImage(bitMatrix);  
	         //输出二维码图片流  
	         try {  
	             ImageIO.write(image, "png", response.getOutputStream());  
	         } catch (IOException e) {  
	             e.printStackTrace();  
	         }
	     } catch (WriterException e1) {  
	         e1.printStackTrace();  
	     }           
	}
	
	/**
     * 获取本机IP地址
     * @return IP
     */
	public static String getRemortIP(HttpServletRequest request) {  
        if (request.getHeader("x-forwarded-for") == null) {  
            return request.getRemoteAddr();  
        }  
        return request.getHeader("x-forwarded-for");  
    } 
	
	/**
     * 微信支付签名算法sign
     */
    private String getSign(Map<String,Object> map) {
        StringBuffer sb = new StringBuffer();
        String[] keyArr = (String[]) map.keySet().toArray(new String[map.keySet().size()]);//获取map中的key转为array
        Arrays.sort(keyArr);//对array排序
        for (int i = 0, size = keyArr.length; i < size; ++i) {
            if ("sign".equals(keyArr[i])) {
                continue;
            }
            sb.append(keyArr[i] + "=" + map.get(keyArr[i]) + "&");
        }
        sb.append("key=" + weixinpayProperties.getKey());
        String sign = string2MD5(sb.toString());
        return sign;
    }
    
    /***
     * MD5加码 生成32位md5码
     */
    private String string2MD5(String str){
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**  
     * 是否签名正确,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。  
     * @return boolean  
     */    
    public static boolean isTenpaySign(String characterEncoding, SortedMap<Object, Object> packageParams, String API_KEY) {    
        StringBuffer sb = new StringBuffer();    
        Set es = packageParams.entrySet();    
        Iterator it = es.iterator();    
        while(it.hasNext()) {    
            Map.Entry entry = (Map.Entry)it.next();    
            String k = (String)entry.getKey();    
            String v = (String)entry.getValue();    
            if(!"sign".equals(k) && null != v && !"".equals(v)) {    
                sb.append(k + "=" + v + "&");    
            }    
        }    
        sb.append("key=" + API_KEY);    
            
        //算出摘要    
        String mysign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toLowerCase();    
        String tenpaySign = ((String)packageParams.get("sign")).toLowerCase();    
            
        return tenpaySign.equals(mysign);    
    }  
    
    /** 
	   * 类型转换 
	   * @author chenp 
	   * @param matrix 
	   * @return 
	   */  
	 public static BufferedImage toBufferedImage(BitMatrix matrix) {  
        int width = matrix.getWidth();  
        int height = matrix.getHeight();  
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);  
        for (int x = 0; x < width; x++) {  
            for (int y = 0; y < height; y++) {  
                image.setRGB(x, y, matrix.get(x, y) == true ? 0xff000000 : 0xFFFFFFFF);  
            }  
        }  
        return image;  
	 }  
	
	/**
	 * 通过返回IO流获取支付地址
	 * @param in
	 * @return
	 */
	private String getElementValue(InputStream in,String key)throws Exception{
		SAXReader reader = new SAXReader();
      Document document = reader.read(in);
      Element root = document.getRootElement();
      List<Element> childElements = root.elements();
      for (Element child : childElements) {
      	if(key.equals(child.getName())){
      		return child.getStringValue();
      	}
      }
      return null;
	}
	
	
}
