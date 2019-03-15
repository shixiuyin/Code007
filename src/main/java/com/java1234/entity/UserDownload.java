package com.java1234.entity;

import java.util.Date;

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
 * 用户下载实体
 * @author java1234 小锋 老师
 *
 */
@Entity
@Table(name="t_userDownload")
public class UserDownload {

	@Id
	@GeneratedValue
	private Integer id; // 编号
	
	@ManyToOne
	@JoinColumn(name="articleId")
	private Article article; // 下载资源
	
	@ManyToOne
	@JoinColumn(name="userId")
	private User user; // 下载用户
	
	@Temporal(TemporalType.TIMESTAMP) 
	private Date downloadDate; // 下载日期

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@JsonSerialize(using=CustomDateTimeSerializer.class)
	public Date getDownloadDate() {
		return downloadDate;
	}

	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}
	
	
}
