package com.java1234.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 用户实体
 * @author Administrator
 *
 */
@Entity
@Table(name="t_user")
public class User implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Integer id; // 编号
	
	@NotEmpty(message="请输入用户名！") 
	@Column(length=100)
	private String userName; // 用户名
	
	@NotEmpty(message="请输入密码！") 
	@Column(length=100)
	private String password; // 密码
	
	
	@Email(message="邮箱地址格式有误！")
	@NotEmpty(message="请输入邮箱地址！") 
	@Column(length=100)
	private String email; // 验证邮箱地址
	
	@Column(length=100)
	private String imageName; // 用户头像
	
	private Integer points=0; // 积分
	
	private boolean isVip=false; // 是否是vip
	
	private boolean isOff=false; // 是否被封禁
	
	private String roleName="会员"; // 角色名称  会员   管理员 两种 默认会员
	
	private Date registerDate; // 注册日期
	
	@Transient
	private Integer messageCount;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName.trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password.trim();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email.trim();
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public boolean isVip() {
		return isVip;
	}

	public void setVip(boolean isVip) {
		this.isVip = isVip;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Integer getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(Integer messageCount) {
		this.messageCount = messageCount;
	}

	public boolean isOff() {
		return isOff;
	}

	public void setOff(boolean isOff) {
		this.isOff = isOff;
	}

	@JsonSerialize(using=CustomDateTimeSerializer.class)
	public Date getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}
	
	
	
}
