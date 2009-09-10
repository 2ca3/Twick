package com.appspot.twick;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import twitter4j.http.AccessToken;




/**
 * @author 2ca3
 * 
 */
public class TwickDwr {
	private static Log log = LogFactory.getLog(TwickDwr.class);


	
	/**
	 * @param page
	 * @return Mentions As JsonStr
	 */
	public String getMentionsAsJsonStr(int page) {
		return getMentionsAsObject(page).toString();
	}
	
	/**
	 * @param page
	 * @return Favorites As JsonStr
	 */
	public String getFavoritesAsJsonStr(int page) {
		return getFavoritesAsObject(page).toString();
	}

	/**
	 * @param page
	 * @return MyTimeline As JsonStr
	 */
	public String getMyTimelineAsJsonStr(int page) {
		return getUserTimelineAsObject(page).toString();
	}

	/**
	 * @param page 
	 * @return FriendsTimeline As JsonStr
	 */
	public String getFriendsTimelineAsJsonStr(int page) {
		return getFriendsTimelineAsObject(page).toString();
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
	public boolean createFavorite(long id){
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
	private JSONArray getFriendsTimelineAsObject(int page) {
		Twitter twitter = null;
		try {
			twitter = getTwitter();
		} catch (Throwable e) {
			log.error(e, e);
		}

		try {
			List<Status> statuses = twitter.getFriendsTimeline(new Paging(page));
			List<Map<String, String>> friendsStatuses = new ArrayList<Map<String, String>>(statuses.size());
			for (Status status : statuses) {
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
	private Twitter getTwitter(){
		return (Twitter) WebContextFactory.get().getHttpServletRequest().getAttribute(Twitter.class.getSimpleName());
	}
	
	private Map<String, String> getMapByStatuses(Status status){
		Map<String, String> map = new HashMap<String, String>(8);
		map.put("id", Long.toString(status.getId()));
		map.put("replyToStatusId", Long.toString(status.getInReplyToStatusId()));
		map.put("name", status.getUser().getName());
		map.put("screenName", status.getUser().getScreenName());
		map.put("profileImageURL", status.getUser().getProfileImageURL().toString());
		map.put("text", status.getText());
		map.put("source", status.getSource());
		map.put("createdAt",  DateFormatUtils.format(new Date((status.getCreatedAt().getTime() + (9 * 3600 * 1000))), "MM/dd HH:mm"));
		return map;
	}

}