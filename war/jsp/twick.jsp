<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ page import="twitter4j.User" %><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ja" xml:lang="ja">
  <head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<meta http-equiv="content-script-type" content="text/javascript" />
	<meta http-equiv="content-style-type" content="text/css" />
	<title>Twick</title>
	<script type="text/javascript" src="js/dwr/interface/TwickDwr.js"></script>
	<script type="text/javascript" src="js/dwr/engine.js"></script>
	<script type="text/javascript" src="js/dwr/util.js"></script>
	<script type="text/javascript" src="js/jquery-1.3.2.js"></script>
	<script type="text/javascript" src="js/jquery-ui-1.7.2.custom.js"></script>
	<script type="text/javascript" src="js/jquery-jtemplates.js"></script>
	<script type="text/javascript" src="js/iphone-style-checkboxes.js"></script>
	<script type="text/javascript" src="js/jquery.cookie.js"></script>
	<script type="text/javascript" src="js/thickbox.js"></script>

	<link type="text/css" href="css/twick.css" rel="stylesheet" />
	<link type="text/css" href="css/iphone-style-checkboxes/style.css" rel="stylesheet" media="screen" />
	<link type="text/css" href="css/ui-lightness/jquery-ui-1.7.2.custom.css" rel="stylesheet" />
	<link type="text/css" href="css/thickbox/thickbox.css" rel="stylesheet" />
<% User user = (User)request.getAttribute("user"); %>
  <style>
	body{ font: 62.5% "Trebuchet MS", sans-serif; margin: 10px;
	  color: #<%= user.getProfileTextColor() %>;
	  background-repeat: repeat;
	  background-color: #<%= user.getProfileBackgroundColor() %>;
	  background-image: url(<%= user.getProfileBackgroundImageUrl() %>);
/*	  	  background-image: url('images/stripe.png'); */
	}
		ul#icons {margin: 0; padding: 0;}
		ul#icons li {margin: 2px; position: relative; padding: 4px 0; cursor: pointer; float: left;  list-style: none;}
		ul#icons span.ui-icon {float: left; margin: 0 4px;}
		a:link {/*未訪問のリンク*/
		  color: #<%= user.getProfileLinkColor() %>;
		}
	div#foot{
		background-color: #ffffff;
	}
  </style>

  <script type="text/javascript">
	 $(document).ready(function(){
		$(function(){
			// Tabs
	        $("div.recent_result").setTemplateURL("js/jtl/timeline.jtl");

			$tabs = $('#tabs').tabs();
			Twick.dspTimeLine($tabs.tabs('option', 'selected'),1);

			$('#tabs').bind('tabsselect', function(event, ui) {
				Twick.dspTimeLine(ui.index,1);
			});

			window.setInterval(function(){
				Twick.dspTimeLine($tabs.tabs('option', 'selected'),$('#page').text());
			},60000);

			//hover states on the static widgets
	  		 $('#dialog_link, ul#icons li').hover(
				 function() { $(this).addClass('ui-state-hover'); }, 
				 function() { $(this).removeClass('ui-state-hover'); }
			 );
		});

		if($.cookie('at')){
			$('#autoAuth').attr('checked',true);
		}
		$('#autoAuth').iphoneStyle();
		$('#autoAuth').iphoneStyle({
			checkedLabel: 'YES',
			uncheckedLabel: 'NO'
		});

		$("div.timeline_tbl").height(Twick.height() - 100).css({cursor:"auto"});

		Twick.dspPage(1);

	});

	$(window).resize(function(){
		$("div.timeline_tbl").height(Twick.height() - 100).css({cursor:"auto"});
	});

	var Twick={
		height:function (){
			if(jQuery.browser.opera) { //Opera (document.documentElement.clientHeightが存在するため先に処理)
				var _height = document.body.clientHeight;
			} else if(typeof document.documentElement.clientHeight == 'number') { //Firefox IE Safari3
				var _height = document.documentElement.clientHeight;
			} else if(typeof window.innerHeight == 'number') {　//Safari2
				var _height = window.innerHeight;
			} else {　//その他のブラウザの場合
				var _height = 600;
			}
			return _height;
		},

		dspTimeLine:function(selected,page)
		{
			if(selected == 0){
				TwickDwr.getFriendsTimelineAsJsonStr(page,Twick.timelineCallBack);
			}else if(selected == 1){
				TwickDwr.getMentionsAsJsonStr(page, Twick.timelineCallBack);
			}else if(selected == 2){
				TwickDwr.getMyTimelineAsJsonStr(page, Twick.timelineCallBack);
			}else if(selected == 3){
				TwickDwr.getFavoritesAsJsonStr(page, Twick.timelineCallBack);
			}
			Twick.dspPage(page);
		},

		timelineCallBack:function(data)
	    {
	      if (data != null && typeof data == 'object'){
	        alert(dwr.util.toDescriptiveString(data, 2));
	      }else{
	        $("div.recent_result").processTemplate(eval(data));
	        $("div.timeline_tbl").height(Twick.height() - 100).css({cursor:"auto"});
	      }
	    },

		inputCallBack:function(data)
		{
		  if (data != null && typeof data == 'object'){
		     alert(dwr.util.toDescriptiveString(data, 2));
		  }else{
		     TwickDwr.getFriendsTimelineAsJsonStr(1, Twick.timelineCallBack);
		     $('#input').val('');
		  } 
		},

		favoriteCallBack:function(data)
		{
		  if (data != null && typeof data == 'object'){
		     alert(dwr.util.toDescriptiveString(data, 2));
		  }else{
		     TwickDwr.getFriendsTimelineAsJsonStr(1, Twick.timelineCallBack);
		     alert("お気に入りに登録しました");
		  } 
		},
		
		autoAuthCallBack:function(data)
		{
			if (data != null && typeof data == 'object') alert(dwr.util.toDescriptiveString(data, 2));
			else if (dwr.util.toDescriptiveString(data, 1) == false) dwr.util.setValue('autoAuthResult', '失敗');
		},

		dspPage:function(page)
		{
			$("#page").text(page);
			if(page == 1){
				$("#page_b").text('＜前のページ　');
			}else{
				$("#page_b").html('<a href="javascript:void(0)" onclick="Twick.dspTimeLine('+ $('#tabs').tabs().tabs('option', 'selected') + ',' + (parseInt(page)-1) + '); return false;">＜前のページ</a>　');
			}
			$("#page_n").html('<a href="javascript:void(0)" onclick="Twick.dspTimeLine('+ $('#tabs').tabs().tabs('option', 'selected') + ',' + (parseInt(page)+1) + '); return false;">　次のページ＞</a>　');
		}
	}
  </script>
  </head>
  <body>
  <!-- つぶやく -->
  <font size="3" color="#ffffff"><strong>いまなにしてる？</strong></font><input class='itext' type='text' size='120' value='' id='input' maxlength="140" onKeyup="$('#count').text(140 - this.value.length)" />
  <input class='ibutton' type='button' onclick='TwickDwr.executeStatus($("#input").val(), Twick.inputCallBack);' value='投稿する' /><span id="count" style="font-size:16px;color:#ffffff;">140</span>
	
	<!-- Tabs --> 
		<div id="tabs"> 
			<ul> 
				<li><a href="#tabs-1">Recent</a></li>
				<li><a href="#tabs-2">Reply</a></li>
				<li><a href="#tabs-3">My</a></li>
				<li><a href="#tabs-4">Favotites</a></li>
			</ul>

			<div id="tabs-1"><div class='recent_result'></div></div> 
			<div id="tabs-2"><div class='recent_result'></div></div> 
			<div id="tabs-3"><div class='recent_result'></div></div>
			<div id="tabs-4"><div class='recent_result'></div></div> 
			<div class="paging">
<span id="page_b"></span>
<span id="page"></span>
<span id="page_n"></span>
</div>

		</div> 
<br />
<div id="foot">
<table>
  <tr>
    <td><a href="/"><img src="images/logo.png" alt="Twick" border="0" /></a></td>
    <td>
<!-- ad -->
<script type="text/javascript"><!--
google_ad_client = "pub-6681409822461545";
/* 468x60, 作成済み 09/09/07 */
google_ad_slot = "5338090892";
google_ad_width = 468;
google_ad_height = 60;
//-->
</script>
<script type="text/javascript"
src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script>
    </td>
    <td>
  <font size="2">自動認証：</font><span id="foo" onclick="if( $('#autoAuth').attr('checked')){TwickDwr.executeAutoAuth(Twick.autoAuthCallBack)}else{TwickDwr.removeAutoAuth(Twick.autoAuthCallBack)}"><input type="checkbox" id="autoAuth" /></span>
  <span id='autoAuthResult' class='reply'></span> 
    </td>
    <td>
    <a href="/info.html" target="_blank">Twick(ついっく)とは？</a>
</tr>
  <tr>
</table>
</div>

</body>
</html>
