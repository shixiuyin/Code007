package com.java1234.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 用户消息实体
 * @author Administrator
 *
 */
@Entity
@Table(name="t_message")
public class Message {

	@Id
	@GeneratedValue
	private Integer id; // 编号
	
	@Column(length=100)
	private String content; // 消息内容
	
	@Temporal(TemporalType.TIMESTAMP) 
	private Date publishDate; // 发布日期时间
	
	@ManyToOne
	@JoinColumn(name="userId")
	private User user; // 所属用户
	
	private boolean isSee=false; // 消息是否被查看  true 是  false 否 

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@JsonSerialize(using=CustomDateTimeSerializer.class)
	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isSee() {
		return isSee;
	}

	public void setSee(boolean isSee) {
		this.isSee = isSee;
	}
	
	
	
	
}
