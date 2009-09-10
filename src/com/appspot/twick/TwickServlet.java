package com.appspot.twick;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author 2ca3
 * 
 */
@SuppressWarnings("serial")
public class TwickServlet extends HttpServlet {
	private static Log log = LogFactory.getLog(TwickServlet.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			// Twitter twitter = (Twitter)
			// session.getAttribute(Twitter.class.getSimpleName());
			Twitter twitter = (Twitter) req.getAttribute(Twitter.class.getSimpleName());
			try {
				req.setAttribute("user", twitter.verifyCredentials());
				getServletContext().getRequestDispatcher("/jsp/twick.jsp").forward(req, resp);
			} catch (ServletException e) {
				log.error(e, e);
			}
			return;
		} catch (TwitterException e) {
			log.error(e, e);
		}
	}
}
