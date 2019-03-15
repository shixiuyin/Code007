package com.java1234.util;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 检查分享链接是否有效
 * @author Administrator
 *
 */
public class CheckShareLinkEnableUtil {

	static CloseableHttpClient httpClient=HttpClients.createDefault();
	/**
	 * 检查链接是否有效
	 * @param link
	 * @return
	 */
	public static boolean check(String link)throws Exception{
		HttpGet httpget = new HttpGet(link); 
		httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0"); // 设置请求头消息User-Agent
		CloseableHttpResponse response=httpClient.execute(httpget);
		HttpEntity entity=response.getEntity(); // 获取返回实体  
		String result=EntityUtils.toString(entity, "utf-8");
		if(result.contains("请输入提取码")||result.contains("分享无限制")){
			return true;
		}else{
			return false;			
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(CheckShareLinkEnableUtil.check("https://pan.baidu.com/s/1phM4JTJVTl8SFrfU2JAf0Q"));
		System.out.println(CheckShareLinkEnableUtil.check("https://pan.baidu.com/s/1phM4JTJVTl8SFrfU2JAf0Q2"));
	}
	
}
