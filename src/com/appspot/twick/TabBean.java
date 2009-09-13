package com.appspot.twick;

import java.util.ArrayList;
import java.util.List;

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
public class TabBean {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String tabId;

	@Persistent
	private String screenName;

	@Persistent

	private Integer tabNo;

	@Persistent
	private String name;

	@Persistent
	private String tabName;

	@Persistent
	private List<String> selectScreenNames;

	@Persistent
	private List<String> notSelectScreenNames;

	@Persistent
	private Boolean leaveRecent;

	public String getTabId() {
		return tabId;
	}
	
	public Integer getTabNo() {
		return tabNo;
	}


	public void setTabNo(Integer tabNo) {
		this.tabNo = tabNo;
	}


	public String getTabName() {
		return tabName;
	}


	public void setTabName(String tabName) {
		this.tabName = tabName;
	}


	public List<String> getSelectScreenNames() {
		return selectScreenNames;
	}


	public void setSelectScreenName(List<String> selectScreenNames) {
		this.selectScreenNames = selectScreenNames;
	}


	public List<String> getNotSelectScreenNames() {
		return notSelectScreenNames;
	}


	public void setNotSelectScreenNames(List<String> notSelectScreenNames) {
		this.notSelectScreenNames = notSelectScreenNames;
	}

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

	public Boolean getLeaveRecent() {
		return leaveRecent;
	}

	public void setLeaveRecent(Boolean leaveRecent) {
		this.leaveRecent = leaveRecent;
	}

	/**
	 * 
	 */
	public TabBean(String screenName,Integer tabNo, String name, String tabName, Boolean leaveRecent, List<String> selectScreenNames, List<String> notSelectScreenNames) {
		this.screenName = screenName;
		this.tabNo = tabNo;
		this.name = name;
		this.tabName= tabName;
		this.leaveRecent = leaveRecent;
		this.selectScreenNames = selectScreenNames;
		this.notSelectScreenNames = notSelectScreenNames;

		this.tabId = screenName + "_" + tabNo.toString();
	}

	/**
	 * 
	 */
	public TabBean(String screenName,Integer tabNo) {
		this.screenName = screenName;
		this.tabNo = tabNo;
		this.leaveRecent = true;
		this.tabId = screenName + "_" + tabNo.toString();
		this.selectScreenNames = new ArrayList<String>();
		this.notSelectScreenNames = new ArrayList<String>();
	}

}

