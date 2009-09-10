package com.appspot.twick;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

/**
 * @author 2ca3
 * 
 */
public class OAuthTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Twitter twitter = new Twitter();
			twitter.setOAuthConsumer("KoZEOEJygwBY5DqrEoZxdA", "0ezr70DdvX7pGaIT6EHIIL8XUG17JvuErtAGPEU7M");
			RequestToken requestToken = twitter.getOAuthRequestToken();
			AccessToken accessToken = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (null == accessToken) {
				System.out.println("Open the following URL and grant access to your account:");
				System.out.println(requestToken.getAuthorizationURL());
				System.out.print("Hit enter when it's done.[Enter]:");
				br.readLine();
				try {
					accessToken = requestToken.getAccessToken();
				} catch (TwitterException te) {
					if (401 == te.getStatusCode()) {
						System.out.println("Unable to get the access token.");
					} else {
						te.printStackTrace();
					}
				}
			}
			// 将来の参照用に accessToken を永続化する
			// storeAccessToken(twitter.verifyCredentials().getId() , at);
			Status status = twitter.updateStatus("水曜日は体重測定の日。54kg。先週と変わらず。");
			System.out.println("Successfully updated the status to [" + status.getText() + "].");
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		System.exit(0);
	}
	// private void storeAccessToken(int useId, AccessToken at){
	// //at.getToken() を保存
	// //at.getTokenSecret() を保存
	// }
}
