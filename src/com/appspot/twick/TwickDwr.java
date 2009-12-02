package com.appspot.twick;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.http.AccessToken;

/**
 * @author 2ca3
 * 
 */
public class TwickDwr {
	private static Log log = LogFactory.getLog(TwickDwr.class);

	/**
	 * @param tabNo
	 * @return NoSelectScreenNames As Json
	 */
	public String getNoSelectScreenNamesAsJson(int tabNo) {
		Twitter twitter = (Twitter) WebContextFactory.get().getHttpServletRequest().getAttribute(Twitter.class.getSimpleName());
		try {

			Map<String,Boolean> selectScreenNamesMap = new HashMap<String,Boolean>();
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				TabBean tabBean = pm.getObjectById(TabBean.class, twitter.verifyCredentials().getScreenName() + "_" + String.valueOf(tabNo));
				List<String> screenNames = tabBean.getSelectScreenNames();
				if (screenNames != null) {
					for (String screenName : screenNames) {
						selectScreenNamesMap.put(screenName, true);
					}
				}
			} catch (JDOObjectNotFoundException e) {
			} finally {
				pm.close();
			}
			List<Map<String,String>> noSelectScreenNames = new ArrayList<Map<String,String>>();
			for (User user : twitter.getFriendsStatuses()) {
				if(!selectScreenNamesMap.containsKey(user.getScreenName())){
					Map<String,String> map = new HashMap<String,String>(2);
					map.put("screenName", user.getScreenName());
					map.put("name", user.getName());
					noSelectScreenNames.add(map);					
				}
			}
			return JSONArray.fromObject(noSelectScreenNames).toString();
		} catch (TwitterException e) {
			log.error(e, e);
		}
		return new JSONArray().toString();
	}

	/**
	 * @param tabNo
	 * @return SelectScreenNames As Json
	 */
	public String getSelectScreenNamesAsJson(int tabNo) {
		Twitter twitter = (Twitter) WebContextFactory.get().getHttpServletRequest().getAttribute(Twitter.class.getSimpleName());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			TabBean tabBean = pm.getObjectById(TabBean.class, twitter.verifyCredentials().getScreenName() + "_" + String.valueOf(tabNo));
			List<Map<String,String>> selectScreenNames = new ArrayList<Map<String,String>>(tabBean.getSelectScreenNames().size());
			for(String screenName : tabBean.getSelectScreenNames()){
				Map<String,String> map = new HashMap<String,String>(2);
				map.put("screenName", screenName);
				map.put("name", twitter.showUser(screenName).getName());
				selectScreenNames.add(map);					
			}
			return JSONArray.fromObject(selectScreenNames).toString();
			
		} catch (JDOObjectNotFoundException e) {
		} catch (TwitterException e) {
			log.error(e, e);
		} finally {
			pm.close();
		}
		return new JSONArray().toString();
	}

	/**
	 * @param tabNo
	 * @param screenName
	 * @return 成功失敗
	 */
	public boolean removeSelectScreenName(int tabNo, String screenName) {
		Twitter twitter = (Twitter) WebContextFactory.get().getHttpServletRequest().getAttribute(Twitter.class.getSimpleName());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			TabBean tabBean = null;
			try {
				tabBean = pm.getObjectById(TabBean.class, twitter.verifyCredentials().getScreenName() + "_" + String.valueOf(tabNo));
			} catch (JDOObjectNotFoundException e) {
				return false;
			}

			List<String> select = tabBean.getSelectScreenNames();
			if (select.remove(screenName)) {
				tabBean.setSelectScreenName(select);
				pm.makePersistent(tabBean);
			}

			if (tabBean.getLeaveRecent()) {
				TabBean tabBeanRecent = null;
				try {
					tabBeanRecent = pm.getObjectById(TabBean.class, twitter.verifyCredentials().getScreenName() + "_1");
				} catch (JDOObjectNotFoundException e) {
					tabBeanRecent = new TabBean(twitter.verifyCredentials().getScreenName(), 1);
				}
				List<String> notSelect = tabBeanRecent.getNotSelectScreenNames();
				if (notSelect.remove(screenName)) {
					tabBeanRecent.setNotSelectScreenNames(notSelect);
					pm.makePersistent(tabBeanRecent);
				}
			}

			return true;
		} catch (TwitterException e) {
			log.error(e, e);
		} finally {
			pm.close();
		}
		return false;
	}

	/**
	 * 振分けScreenNameの追加
	 * 
	 * @param tabNo
	 * @param screenName
	 * @param leaveRecent
	 * @return 成功失敗
	 */
	public boolean addSelectScreenName(int tabNo, String screenName, boolean leaveRecent) {
		Twitter twitter = (Twitter) WebContextFactory.get().getHttpServletRequest().getAttribute(Twitter.class.getSimpleName());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			TabBean tabBean = null;
			try {
				tabBean = pm.getObjectById(TabBean.class, twitter.verifyCredentials().getScreenName() + "_" + String.valueOf(tabNo));
			} catch (JDOObjectNotFoundException e) {
				tabBean = new TabBean(twitter.verifyCredentials().getScreenName(), tabNo);
			}

			List<String> select = tabBean.getSelectScreenNames();
			if (!select.contains(screenName)) {
				select.add(screenName);
				tabBean.setSelectScreenName(select);
				pm.makePersistent(tabBean);
			}

			if (!leaveRecent) {
				TabBean tabBeanRecent = null;
				try {
					tabBeanRecent = pm.getObjectById(TabBean.class, twitter.verifyCredentials().getScreenName() + "_1");
				} catch (JDOObjectNotFoundException e) {
					tabBeanRecent = new TabBean(twitter.verifyCredentials().getScreenName(), 1);
				}
				List<String> notSelect = tabBeanRecent.getNotSelectScreenNames();
				if (!notSelect.contains(screenName)) {
					notSelect.add(screenName);
					tabBeanRecent.setNotSelectScreenNames(notSelect);
					pm.makePersistent(tabBeanRecent);
				}
			}
			return true;
		} catch (TwitterException e) {
			log.error(e, e);
		} finally {
			pm.close();
		}
		return false;
	}

	/**
	 * @param page
	 * @return Mentions As Json
	 */
	public String getMentionsAsJson(int page) {
		return getMentionsAsObject(page).toString();
	}

	/**
	 * @param page
	 * @return Favorites As Json
	 */
	public String getFavoritesAsJson(int page) {
		return getFavoritesAsObject(page).toString();
	}

	/**
	 * @param page
	 * @return MyTimeline As Json
	 */
	public String getMyTimelineAsJson(int page) {
		return getUserTimelineAsObject(page).toString();
	}

	/**
	 * @param page
	 * @param tabNo
	 * @return FriendsTimeline As Json
	 */
	public String getFriendsTimelineAsJson(int page, int tabNo) {
		return getFriendsTimelineAsObject(page, tabNo).toString();
	}

	/**
	 * @param text
	 * @return つぶやき成功失敗
	 */
	public boolean executeStatus(String text) {
		Twitter twitter = (Twitter) WebContextFactory.get().getHttpServletRequest().getAttribute(Twitter.class.getSimpleName());
		try {
			twitter.updateStatus(text);
		} catch (TwitterException e) {
			log.error(e, e);
			return false;
		}
		return true;
	}

	/**
	 * @return 設定変更成功失敗
	 */
	public boolean executeAutoAuth() {
		try {
			WebContext webContext = WebContextFactory.get();
			HttpSession session = webContext.getSession();

			AccessToken accessToken = (AccessToken) session.getAttribute(AccessToken.class.getSimpleName());
			Cookie cookieAT = new Cookie("at", AuthFilter.encrypt(accessToken.getToken()) + "_" + AuthFilter.encrypt(accessToken.getTokenSecret()));
			cookieAT.setPath("/");
			cookieAT.setMaxAge(60 * 60 * 24 * 7);
			webContext.getHttpServletResponse().addCookie(cookieAT);
		} catch (Exception e) {
			log.error(e, e);
			return false;
		}
		return true;
	}

	/**
	 * @return 設定変更成功失敗
	 */
	public boolean removeAutoAuth() {
		try {
			WebContext webContext = WebContextFactory.get();
			Cookie cookieAT = AuthFilter.getCookie(webContext.getHttpServletRequest(), "at");
			if (cookieAT != null) {
				cookieAT.setValue("");
				cookieAT.setPath("/");
				cookieAT.setMaxAge(0);
				webContext.getHttpServletResponse().addCookie(cookieAT);
			}
		} catch (Exception e) {
			log.error(e, e);
			return false;
		}
		return true;

	}

	/**
	 * @param id
	 * @return 成功失敗
	 */
	public boolean createFavorite(long id) {
		Twitter twitter = (Twitter) WebContextFactory.get().getHttpServletRequest().getAttribute(Twitter.class.getSimpleName());
		try {
			twitter.createFavorite(id);
		} catch (TwitterException e) {
			log.error(e, e);
			return false;
		}
		return true;
	}

	/**
	 * @return FriendsTimeline As Object
	 */
	private JSONArray getFriendsTimelineAsObject(int page, int tabNo) {
		Twitter twitter = null;
		try {
			twitter = getTwitter();
		} catch (Throwable e) {
			log.error(e, e);
		}

		Map<String, Boolean> selectMap = new HashMap<String, Boolean>();
		Map<String, Boolean> notSelectMap = new HashMap<String, Boolean>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			TabBean tabBean = pm.getObjectById(TabBean.class, twitter.verifyCredentials().getScreenName() + "_" + String.valueOf(tabNo));
			if (tabBean != null){
				if (tabNo >= 5 && tabBean.getSelectScreenNames() != null) {
					for (String screenName : tabBean.getSelectScreenNames()) {
						selectMap.put(screenName, true);
					}
				}
				if (tabNo <= 4 && tabBean.getNotSelectScreenNames() != null) {
					for (String screenName : tabBean.getNotSelectScreenNames()) {
						notSelectMap.put(screenName, true);
					}
				}
			}
		} catch (JDOObjectNotFoundException e) {
			// 存在しない場合は、振分けなし
		} catch (TwitterException e) {
			log.error(e, e);
			return new JSONArray();
		} finally {
			pm.close();
		}

		try {
			List<Status> statuses = twitter.getFriendsTimeline(new Paging(page));
			List<Map<String, String>> friendsStatuses = new ArrayList<Map<String, String>>(statuses.size());
			for (Status status : statuses) {
				if (tabNo >= 5 && !selectMap.containsKey(status.getUser().getScreenName())) {
					// 振分け対象外
					continue;
				}
				if (tabNo <= 4 && notSelectMap.containsKey(status.getUser().getScreenName())) {
					// 除外対象
					continue;
				}
				friendsStatuses.add(getMapByStatuses(status));
			}
			return JSONArray.fromObject(friendsStatuses);
		} catch (Exception e) {
			log.error(e, e);
		}
		return new JSONArray();
	}

	private JSONArray getUserTimelineAsObject(int page) {
		Twitter twitter = null;
		try {
			twitter = getTwitter();
		} catch (Throwable e) {
			log.error(e, e);
		}

		try {
			List<Status> statuses = twitter.getUserTimeline(new Paging(page));
			List<Map<String, String>> userStatuses = new ArrayList<Map<String, String>>(statuses.size());
			for (Status status : statuses) {
				userStatuses.add(getMapByStatuses(status));
			}
			return JSONArray.fromObject(userStatuses);
		} catch (Exception e) {
			log.error(e, e);
		}
		return new JSONArray();
	}

	private JSONArray getFavoritesAsObject(int page) {
		Twitter twitter = null;
		try {
			twitter = getTwitter();
		} catch (Throwable e) {
			log.error(e, e);
		}

		try {
			List<Status> statuses = twitter.getFavorites(page);
			List<Map<String, String>> favoritesStatuses = new ArrayList<Map<String, String>>(statuses.size());
			for (Status status : statuses) {
				favoritesStatuses.add(getMapByStatuses(status));
			}
			return JSONArray.fromObject(favoritesStatuses);
		} catch (Exception e) {
			log.error(e, e);
		}
		return new JSONArray();
	}

	private JSONArray getMentionsAsObject(int page) {
		Twitter twitter = null;
		try {
			twitter = getTwitter();
		} catch (Throwable e) {
			log.error(e, e);
		}

		try {
			List<Status> statuses = twitter.getMentions(new Paging(page));
			List<Map<String, String>> mentionStatuses = new ArrayList<Map<String, String>>(statuses.size());
			for (Status status : statuses) {
				mentionStatuses.add(getMapByStatuses(status));
			}
			return JSONArray.fromObject(mentionStatuses);
		} catch (Exception e) {
			log.error(e, e);
		}
		return new JSONArray();
	}

	private Twitter getTwitter() {
		return (Twitter) WebContextFactory.get().getHttpServletRequest().getAttribute(Twitter.class.getSimpleName());
	}

	private Map<String, String> getMapByStatuses(Status status) {
		Map<String, String> map = new HashMap<String, String>(8);
		map.put("id", Long.toString(status.getId()));
		map.put("replyToStatusId", Long.toString(status.getInReplyToStatusId()));
		map.put("name", status.getUser().getName());
		map.put("screenName", status.getUser().getScreenName());
		map.put("profileImageURL", status.getUser().getProfileImageURL().toString());
		map.put("text", status.getText());
		map.put("source", status.getSource());
		map.put("createdAt", DateFormatUtils.format(new Date((status.getCreatedAt().getTime() + (9 * 3600 * 1000))), "MM/dd HH:mm"));
		return map;
	}

}