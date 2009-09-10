package com.appspot.twick;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * 
 * @author 2ca3
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class AuthBean {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String screenName;

	@Persistent
	private String name;

	@Persistent
	private Date authAt;

	/**
	 * @return screenName
	 */
	public String getScreenName() {
		return screenName;
	}


	/**
	 * @param screenName
	 */
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}


	/**
	 * @return userName
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return authAt
	 */
	public Date getAuthAt() {
		return authAt;
	}

	/**
	 * @param authAt
	 */
	public void setAuthAt(Date authAt) {
		this.authAt = authAt;
	}

	/**
	 * @param screenName
	 * @param name
	 * @param authAt
	 */
	public AuthBean(String screenName, String name, Date authAt) {
		this.screenName = screenName;
		this.name = name;
		this.authAt = authAt;
	}
	
	/**
	 * 
	 */
	public AuthBean() {
	}}
