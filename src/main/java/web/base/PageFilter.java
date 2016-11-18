package web.base;

import com.sogou.ms.util.ServletUtil;
import com.sogou.ms.util._;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerMapping;
import web.util.ReqUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.sogou.ms.util._.find;
import static com.sogou.ms.util._.trimToEmpty;

/**
 * Created by Jarod on 2015/10/22.
 */
public class PageFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        long start = System.currentTimeMillis();

        boolean isStaticFile = isStaticFile(req.getRequestURI());
        if (!isStaticFile) {
            wrapRequest(req, resp);
        }

        chain.doFilter(req, resp);

        if (!isStaticFile)
            opLog(start, req);
    }

    private void wrapRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setHeader(req, resp);
    }


    private static void handleWUID(HttpServletRequest req, HttpServletResponse resp) {

    }

    public static boolean isStaticFile(String uri) {
        String suffix = trimToEmpty(find(suffixPattern, uri)).toLowerCase();
        return staticSuffixes.contains(suffix);
    }

    static final Pattern suffixPattern = Pattern.compile("\\.(\\w+)$");
    static final Set<String> staticSuffixes = new HashSet<String>(Arrays.asList( //
            "ico", "css", "js", "gif", "png", "jpg", "jpeg", "swf", "txt"));

    private void setHeader(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }


    public static void opLog(long start, HttpServletRequest req) {
        long end = System.currentTimeMillis();
        String url = ServletUtil.getUrl(req);
        String ip = ServletUtil.getIp(req);
        int cost = (int) (end - start);
        String type = "geri";
        String log = _.f("[Sogou-Observer,type=%s,cost=%s,Owner=OP,url=%s,ip=%s]", type, cost, url, ip);
        op.info(log);

        /**********增加AutoLog逻辑**************/
        autoLog(start,req);
    }

    private static void autoLog(long start,HttpServletRequest req){

    }

    private static Logger op = LoggerFactory.getLogger("OP");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
