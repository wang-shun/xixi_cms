package web.util;

import com.sogou.ms.util._;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;

/**
 * User: madtracy
 * Time: 2016/5/26.
 */
public class ElUtil {
	public static String fs(String str, int length){
		if(_.isEmpty(str))
			return "";
		int size = str.length();
		return size<=length?str:(str.substring(0,length)+"...");
	}

	public static boolean contains(String str, String str2){
		if(_.isEmpty(str))
			return false;
		return str.contains(str2);
	}


	public static final String escapeHtml(String html){
		return _htmlTranslator.translate(String.valueOf(html));
	}

	private static final CharSequenceTranslator _htmlTranslator = new LookupTranslator(new String[][] {
			{ "<","&lt;"},
			{ ">","&gt;" },
			{ "\"","&#34;" },
			{ "'","&#39;" },
			{"\n", ""},
			{"+", "&#43;"}
	});

}
