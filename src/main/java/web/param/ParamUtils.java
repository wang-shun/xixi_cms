package web.param;

import com.sogou.ms.util.ServletUtil;
import com.sogou.ms.util._;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ParamUtils {
    private static final Map<Pattern, String> URLParamMapping = new HashMap<>();
    private static final Set<Pattern> IMPORTANTURL = new HashSet<>();

    static {
        URLParamMapping.put(Pattern.compile("/h5/cpt/detail"), "detail");
        URLParamMapping.put(Pattern.compile("/h5/err-chapter"), "errc");
        URLParamMapping.put(Pattern.compile("/h5/boy"), "boy");
        URLParamMapping.put(Pattern.compile("/h5/category"), "cat");
        URLParamMapping.put(Pattern.compile("/h5/discount"), "discount");
        URLParamMapping.put(Pattern.compile("/h5/nobook"), "nobook");
        URLParamMapping.put(Pattern.compile("/h5/finish"), "finish");
        URLParamMapping.put(Pattern.compile("/h5/free"), "free");
        URLParamMapping.put(Pattern.compile("/h5/girl"), "girl");
        URLParamMapping.put(Pattern.compile("/h5/index"), "idx");
        URLParamMapping.put(Pattern.compile("/h5/rank"), "rank");
        URLParamMapping.put(Pattern.compile("/h5/ranking"), "ranking");
        URLParamMapping.put(Pattern.compile("/h5/sort"), "sort");
        URLParamMapping.put(Pattern.compile("/h5/terms"), "terms");
        URLParamMapping.put(Pattern.compile("/h5/update"), "update");
        URLParamMapping.put(Pattern.compile("/h5/search"), "search");
        URLParamMapping.put(Pattern.compile("/h5/shelf"), "shelf");
        URLParamMapping.put(Pattern.compile("/h5/buy/list"), "blist");
        URLParamMapping.put(Pattern.compile("/h5/recharge"), "recharge");
        URLParamMapping.put(Pattern.compile("/h5/recharge/list"), "rlist");
        URLParamMapping.put(Pattern.compile("/h5/user"), "user");


        IMPORTANTURL.add(Pattern.compile("/s/v[25]/top"));
    }

    public static final boolean isImportantPath(String path) {
        for (Pattern urlPattern : IMPORTANTURL) {
            if (urlPattern.matcher(path).matches())
                return true;
        }
        return false;
    }

    public static final String shortName(String path) {
        if (path != null)
            for (Pattern urlPattern : URLParamMapping.keySet())
                if (urlPattern.matcher(path).matches())
                    return URLParamMapping.get(urlPattern);
        return "other";
    }

    public static final String null2Empty(String p) {
        if (p == null) return "";
        return p;
    }

    public static String getSgid(HttpServletRequest request) {
        String sgid = ServletUtil.getCookie(request, "sgid");
        if (isValidSgid(sgid))
            return sgid;

        /*
        2016-7-15 12:38:53 考虑到geri有用户付费的逻辑，还是尽量使用cookie的sgid，不要使用url的sgid，避免分享链接造成问题
        sgid = request.getParameter("sgid");
        if (isValidSgid(sgid))
            return sgid;
        */

        return null;
    }

    private static boolean isValidSgid(String sgid) {
        return _.nonEmpty(sgid) && !"0".equals(sgid);
    }

    public static String getUsid(HttpServletRequest request) {
        UsidParam usidParam = UsidParam.of(request);
        if (usidParam != null)
            return _.trimToEmpty(usidParam.value());
        return "";
    }

    public static String getGf(HttpServletRequest request) {
//        if (statParam != null)
//            return _.trimToEmpty(statParam.value());
        return "";
    }
}
