package com.java1234.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;




/**
 * 资源实体
 * @author java1234 小锋 老师
 *
 */
@Entity
@Table(name="t_article")
public class Article implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Integer id; // 编号
	
	@Column(length=200)
	private String name; // 资源名称
	
	@Temporal(TemporalType.TIMESTAMP) 
	private Date publishDate; // 发布日期
	
	@Transient
	private String publishDateStr; // 发布日期字符串
	
	@ManyToOne
	@JoinColumn(name="userId")
	private User user; // 所属用户
	
	@ManyToOne
	@JoinColumn(name="typeId")
	private ArcType arcType; // 所属资源类型
	
	private Integer points; // 积分
	
	@Lob
    @Column(columnDefinition="text")
	private String content; // 资源描述
	
	@Column(length=200)
	private String download1; // 百度云地址 下载地址1 用户地址
	
	@Column(length=10)
	private String password1; // 密码1
	
	
	private boolean isHot=false; // 是否是热门资源  true 是  false 否
	
	private Integer state; // 审核状态 1 未审核  2 审核通过 3 审核未通过
	private String reason; // 审核未通过原因
	private Date checkDate; // 审核日期
	
	private boolean isUseful=true; // 资源链接是否有效 true 有效 false 无效 默认有效
	
	private Integer view; // 访问次数
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}


	public ArcType getArcType() {
		return arcType;
	}

	public void setArcType(ArcType arcType) {
		this.arcType = arcType;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDownload1() {
		return download1;
	}

	public void setDownload1(String download1) {
		this.download1 = download1;
	}


	public boolean isHot() {
		return isHot;
	}

	public void setHot(boolean isHot) {
		this.isHot = isHot;
	}

	@JsonSerialize(using=CustomDateTimeSerializer.class)
	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public String getPublishDateStr() {
		return publishDateStr;
	}

	public void setPublishDateStr(String publishDateStr) {
		this.publishDateStr = publishDateStr;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@JsonSerialize(using=CustomDateTimeSerializer.class)
	public Date getCheckDate() {
		return checkDate;
	}

	public void setCheckDate(Date checkDate) {
		this.checkDate = checkDate;
	}

	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password1) {
		this.password1 = password1;
	}

	public boolean isUseful() {
		return isUseful;
	}

	public void setUseful(boolean isUseful) {
		this.isUseful = isUseful;
	}

	public Integer getView() {
		return view;
	}

	public void setView(Integer view) {
		this.view = view;
	}

	
	
	
	
}
