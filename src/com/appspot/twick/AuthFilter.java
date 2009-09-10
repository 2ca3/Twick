package com.appspot.twick;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

/**
 * @author 2ca3
 * 
 */
public class AuthFilter implements Filter {

	private static final Log log = LogFactory.getLog(AuthFilter.class);
	private static String consumerKey;
	private static String consumerSecret;
	private static String cryptKey;

	@Override
	public void destroy() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		consumerKey = System.getProperty("twitterConsumerKey");
		consumerSecret = System.getProperty("twitterConsumerSecret");
		cryptKey = System.getProperty("cryptKey");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpSession session = ((HttpServletRequest) req).getSession(true);
		Twitter twitter = new Twitter();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);

		// 認可済み
		if (session.getAttribute(AccessToken.class.getSimpleName()) != null) {
			twitter.setOAuthAccessToken((AccessToken) session.getAttribute(AccessToken.class.getSimpleName()));
			req.setAttribute(Twitter.class.getSimpleName(), twitter);
			chain.doFilter(req, resp);
			return;
		}

		// クッキー認可
		Cookie cookieAT = getCookie((HttpServletRequest) req, "at");
		if (cookieAT != null && StringUtils.isNotBlank(cookieAT.getValue())) {
			AccessToken accessToken;
			try {
				String at[] = cookieAT.getValue().split("_");
				accessToken = new AccessToken(decrypt(at[0]), decrypt(at[1]));
				twitter.setOAuthAccessToken(accessToken);
				req.setAttribute(Twitter.class.getSimpleName(), twitter);
				session.setAttribute(AccessToken.class.getSimpleName(), accessToken);
				cookieAT.setMaxAge(60 * 60 * 24 * 7);
				((HttpServletResponse) resp).addCookie(cookieAT);
				chain.doFilter(req, resp);
				return;
			} catch (Exception e) {
				log.error(e, e);
			}
		}

		// Twitterで認可
		if (session.getAttribute(RequestToken.class.getSimpleName()) != null) {
			RequestToken requestToken = (RequestToken) session.getAttribute(RequestToken.class.getSimpleName());
			try {
				AccessToken accessToken = requestToken.getAccessToken();
				twitter.setOAuthAccessToken(accessToken);
				req.setAttribute(Twitter.class.getSimpleName(), twitter);
				session.setAttribute(AccessToken.class.getSimpleName(), accessToken);
				session.removeAttribute(RequestToken.class.getSimpleName());

				PersistenceManager pm = PMF.get().getPersistenceManager();
				try {
					pm.getObjectById(twitter.verifyCredentials().getScreenName());
				} catch (JDOObjectNotFoundException e) {
					AuthBean authBean = new AuthBean(twitter.verifyCredentials().getScreenName(), twitter.verifyCredentials().getName(), new Date());
					pm.makePersistent(authBean);
				}finally{
					pm.close();
				}
				
				// oauth_tokenパラメータがGETで見えているのは良くないのでリダイレクト
				((HttpServletResponse) resp).sendRedirect("/");
			} catch (TwitterException e) {
				if (401 == e.getStatusCode()) {
					log.warn("認可失敗 RequestToken:" + requestToken.toString());
					req.setAttribute("error", "認可に失敗しました。Twitterの画面で<strong>許可する</strong>をクリックして下さい。");
				} else {
					log.error(e, e);
					req.setAttribute("error", "認可に失敗しました。何らかの問題が発生しました。もう一度Twitterの画面で<strong>許可する</strong>をクリックして下さい。");
				}
				try {
					// リクエストトークンを取得し直す
					requestToken = twitter.getOAuthRequestToken();
					session.setAttribute(RequestToken.class.getSimpleName(), requestToken);
					req.setAttribute("authurl", requestToken.getAuthorizationURL());
				} catch (TwitterException e1) {
					log.error(e, e);
				}
				req.getRequestDispatcher("/jsp/auth.jsp").forward(req, resp);
			}
			return;
		}

		// 未認可のため認可画面を出して促す
		RequestToken requestToken = null;
		try {
			requestToken = twitter.getOAuthRequestToken();
			session.setAttribute(RequestToken.class.getSimpleName(), requestToken);
			req.setAttribute("authurl", requestToken.getAuthorizationURL());
		} catch (TwitterException e) {
			log.error(e, e);
			req.setAttribute("error", "認可に失敗しました。何らかの問題が発生しました。画面をリロードして下さい。");
		}
		req.getRequestDispatcher("/jsp/auth.jsp").forward(req, resp);
	}

	/**
	 * @param req
	 * @param name
	 * @return cookie
	 */
	public static Cookie getCookie(HttpServletRequest req, String name) {
		// 保存されているすべてのクッキーの配列を取得します
		Cookie[] cookies = req.getCookies();
		// 目的のクッキーを保存するためのクッキーオブジェクト
		Cookie cookie = null;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				// 名前が"message"のクッキーかどうかチェック
				if (cookies[i].getName().equals(name)) {
					// 該当するクッキーを取得します
					cookie = cookies[i];
				}
			}
		}
		return cookie;
	}

	/**
	 * @param text
	 * @return 暗号化文字列
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static String encrypt(String text) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec sksSpec = new SecretKeySpec(cryptKey.getBytes(), "Blowfish");
		Cipher cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.ENCRYPT_MODE, sksSpec);
		byte[] encrypted = cipher.doFinal(text.getBytes());
		return new String(Hex.encodeHex(encrypted));
	}

	/**
	 * @param text
	 * @return 複合化文字列
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws DecoderException
	 */
	public static String decrypt(String text) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, DecoderException {
		byte[] encrypted = null;

		encrypted = Hex.decodeHex(text.toCharArray());
		SecretKeySpec sksSpec = new SecretKeySpec(cryptKey.getBytes(), "Blowfish");
		Cipher cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.DECRYPT_MODE, sksSpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return new String(decrypted);
	}
}
