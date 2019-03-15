package com.java1234.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.java1234.entity.ArcType;
import com.java1234.service.ArcTypeService;
import com.java1234.service.LinkService;

/**
 * 初始化加载数据
 * @author Administrator
 *
 */
@Component("initSystem")
public class InitSystem implements ServletContextListener,ApplicationContextAware{

	private static ApplicationContext applicationContext;
	
	public static Map<Integer,ArcType> arcTypeMap=new HashMap<Integer,ArcType>();
	
	
	/**
	 * 加载数据到application缓存中
	 * @param application
	 */
	public void loadData(ServletContext application){
		ArcTypeService arcTypeService=(ArcTypeService) applicationContext.getBean("arcTypeService");
		LinkService linkService=(LinkService) applicationContext.getBean("linkService");
		List<ArcType> arcTypeList=arcTypeService.listAll(Sort.Direction.ASC, "sort");
		application.setAttribute("allArcTypeList", arcTypeList); // 所有类别
		for(ArcType arcType:arcTypeList){
			arcTypeMap.put(arcType.getId(), arcType);
		}
		application.setAttribute("linkList", linkService.listAll(Sort.Direction.ASC,"sort")); // 所有友情链接
	}


	@Override
	public void contextInitialized(ServletContextEvent sce) {
		loadData(sce.getServletContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		this.applicationContext=applicationContext;
	}

}
