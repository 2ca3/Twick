<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ page import="org.apache.commons.lang.StringUtils" %><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ja" xml:lang="ja">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
    <meta http-equiv="content-script-type" content="text/javascript" />
    <meta http-equiv="content-style-type" content="text/css" />
	<link type="text/css" href="css/twick.css" rel="stylesheet" />
    <title>Twick:Autn</title>
  <style>
	body{ font: 82.5% "Trebuchet MS", sans-serif; margin: 10px;
	  color: #ffffff;
	  background-repeat: repeat;
	  background-color: #9AE4E8;
	  background-image: url('images/stripe.png');
	}
  </style>
  </head>
  <body>
      <table class="tbl2">
        <tr>
          <td class="image">
              <img alt="Twick" src="images/logo.png" border="0" />
          </td>
        </tr>
        <tr>
          <td class="body">
Twick(ついっく)はブラウザ上で動くTwitterクライアントです。<br>
以下のリンクをクリックして認証して下さい。<br />
<a href="<%= request.getAttribute("authurl") %>"><img src="/images/twitter_logo_header.png" border="0">で認証</a><br />
<!--
<a href="/images/twick_sample.png" target="_blank">画面例</a><br />
βバージョンとして提供しつつ、継続的に機能改善していきます。<br />
TwickはOAuth認証を用いてTwitterの情報を取得します。<a href="http://oauth.net/" target="blank">OAuth認証とは？</a><br />
-->
<font color="red"><%= StringUtils.stripToEmpty((String)request.getAttribute("error")) %><br /></font>
          </td>
        </tr>
<tr>
    <td>
    <a href="/info.html" target="_blank">Twick(ついっく)とは？</a>
    </td>
</tr>

      </table>
  </body>

</html>
