package com.czzz.utils;

import java.text.SimpleDateFormat;

import com.czzz.demo.R;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class TextUtils {

	public static String unicodeToString(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;

	}
	
	public static String removeEndEmptyLines(String input) {
	    return input.replaceFirst("\\n{2,}\\z$", "");
	}

	public static String formatSmartTime(String timeStr) {
		long time = Long.valueOf(timeStr);
		return formatSmartTime(time);
	}
	
	public static String formatHomeSmartTime(String timeStr) {
		long time = Long.valueOf(timeStr);
		return formatSmartTime(time);
	}
	
	public static String formatSmartTime(long time) {
		
		Date now = new Date();
		long day = 0;// 天数
		long hour = 0;// 小时
		long min = 0;// 分钟
		long sec = 0;// 秒
		long current = now.getTime();
		time = time * 1000l;
		Date pre = new Date(time);
		long diff;
		diff = Math.abs(current - time);

		// 将时间差换算成天时分秒
		day = diff / (24 * 60 * 60 * 1000);
		hour = (diff / (60 * 60 * 1000));
		min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
		sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

		if (hour > 23 && day < 6 && day > 2) {
			return day + "天前";
		}

		if (day==0 && hour==0 && min == 0) {
			return sec + "秒前";
		}

		if (day==0 && hour == 0) {
			return min + "分钟前";
		}

		if (day == 0 && hour <= 4) {
			return hour + "小时前";
		}

		SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
		format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

		if (day == 0 && hour > 4 && (pre.getDay() == now.getDay())) {
			format.applyLocalizedPattern("HH:mm");
			return "今天 " + format.format(time);
		}

		if (day < 2
				&& (Math.abs(pre.getDay() - now.getDay()) == 1 || (pre.getDay() == 6 && now
						.getDay() == 0))) {
			format.applyLocalizedPattern("HH:mm");
			return "昨天 " + format.format(time);
		}

		if (pre.getYear() != now.getYear())
			format.applyLocalizedPattern("yyyy年MM月dd日");

		return format.format(time);

	}
	
	public static String formatRefreshTime(long time) {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
		
		return format.format(time*1000l);
	}
	
	/**
	 * chatlist time format
	 * @param time
	 * @return
	 */
	public static String formatChatListTime(long time){
		Date now = new Date();
		long day = 0;// 天数
		long hour = 0;// 小时
		long min = 0;// 分钟
		long sec = 0;// 秒
		long current = now.getTime();
		time = time * 1000l;
		Date pre = new Date(time);
		long diff;
		diff = Math.abs(current - time);

		// 将时间差换算成天时分秒
		day = diff / (24 * 60 * 60 * 1000);
		hour = (diff / (60 * 60 * 1000));
		min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
		sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		
		SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm");
		format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
		
		if (day==0 && hour==0 && min == 0) {
			return "刚刚";
		}
		
		if (day==0 && hour == 0) {
			return min + "分钟前";
		}
		
		if (day == 0 && hour <= 2) {
			return hour + "小时前";
		}
		
		if (day == 0 && (pre.getDay() == now.getDay())) {
			format.applyLocalizedPattern("HH:mm");
			return "今天 " + format.format(time);
		}
		
		if (day < 2
				&& (Math.abs(pre.getDay() - now.getDay()) == 1 || (pre.getDay() == 6 && now
						.getDay() == 0))) {
			format.applyLocalizedPattern("HH:mm");
			return "昨天 " + format.format(time);
		}
		
		if (pre.getYear() != now.getYear())
			format.applyLocalizedPattern("yyyy年MM月dd日 HH:mm");
		
		return format.format(time);
	}
	
	private static final Pattern sharp_Pattern = Pattern.compile("#[^#]+?#");
	
	private static Pattern getSharpPattern() {
		return sharp_Pattern;
	}
	
	public static SpannableString decorateTrendInSpannableString(
			Context con, SpannableString spannableString) {
		CharacterStyle characterStyle = null;
		
		List<Map<String, Object>> list = getStartAndEndIndex(spannableString.toString(), getSharpPattern());
		
		int size = list.size();
		if(list != null && size > 0) {
			for(int i = 0; i < size; i++) {
				characterStyle = new ForegroundColorSpan(con.getResources().getColor(R.color.blue));
				Map<String, Object> map = list.get(i);
				spannableString.setSpan(characterStyle, (Integer)map.get("startIndex"), (Integer)map.get("endIndex"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		
		return spannableString;
	}
	
	private static List<Map<String, Object>> getStartAndEndIndex(String sourceStr, Pattern pattern) {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		
		Matcher matcher = pattern.matcher(sourceStr);
		
		boolean isFind = matcher.find();
		while(isFind) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startIndex", matcher.start());
			map.put("endIndex", matcher.end());
			list.add(map);
			
			isFind = matcher.find((Integer)map.get("endIndex") - 1);
		}
		
		return list;
	}
	

	private static HashMap<String, String> _userLinkMapping = new HashMap<String, String>();
	private static final Pattern NAME_MATCHER = Pattern.compile("@.+?\\s");
	private static final Linkify.MatchFilter NAME_MATCHER_MATCH_FILTER = new Linkify.MatchFilter() {
		@Override
		public final boolean acceptMatch(final CharSequence s, final int start,
				final int end) {

			String name = s.subSequence(start + 1, end).toString().trim();
			boolean result = _userLinkMapping.containsKey(name);
			return result;
		}
	};

	private static final Linkify.TransformFilter NAME_MATCHER_TRANSFORM_FILTER = new Linkify.TransformFilter() {

		@Override
		public String transformUrl(Matcher match, String url) {
			// TODO Auto-generated method stub
			String name = url.subSequence(1, url.length()).toString().trim();
			return _userLinkMapping.get(name);
		}
	};

	private static final String TWITTA_USER_URL = "twitta://users/";

	public static void linkifyUsers(TextView view) {
		Linkify.addLinks(view, NAME_MATCHER, TWITTA_USER_URL,
				NAME_MATCHER_MATCH_FILTER, NAME_MATCHER_TRANSFORM_FILTER);
	}

	public static void setTweetText(TextView textView, String text) {
		String processedText = preprocessText(text);
		textView.setText(Html.fromHtml(processedText), BufferType.SPANNABLE);
		Linkify.addLinks(textView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
		linkifyUsers(textView);
		_userLinkMapping.clear();
	}

	private static Pattern USER_LINK = Pattern
			.compile("@<a href=\"http:\\/\\/fanfou\\.com\\/(.*?)\" class=\"former\">(.*?)<\\/a>");

	private static String preprocessText(String text) {
		// 处理HTML格式返回的用户链接
		Matcher m = USER_LINK.matcher(text);
		while (m.find()) {
			_userLinkMapping.put(m.group(2), m.group(1));
		}

		// 将User Link的连接去掉
		StringBuffer sb = new StringBuffer();
		m = USER_LINK.matcher(text);
		while (m.find()) {
			m.appendReplacement(sb, "@$2");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 从消息中获取全部提到的人，将它们按先后顺序放入一个列表
	 * 
	 * @param msg
	 *            消息文本
	 * @return 消息中@的人的列表，按顺序存放
	 */
	public static List<String> getMentions(String msg) {
		ArrayList<String> mentionList = new ArrayList<String>();

		final Pattern p = Pattern.compile("@(.*?)\\s");
		final int MAX_NAME_LENGTH = 12; // 简化判断，无论中英文最长12个字

		Matcher m = p.matcher(msg);
		while (m.find()) {
			String mention = m.group(1);

			// 过长的名字就忽略（不是合法名字） +1是为了补上“@”所占的长度
			if (mention.length() <= MAX_NAME_LENGTH + 1) {
				// 避免重复名字
				if (!mentionList.contains(mention)) {
					mentionList.add(m.group(1));
				}
			}
		}
		return mentionList;
	}

}
