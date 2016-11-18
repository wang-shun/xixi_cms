package web.controller;

import com.sogou.ms.util.ServletUtil;
import com.sogou.ms.util._;
import com.sogou.ms.util.toolkit.AutoLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;
import web.entity.User;
import web.exception.NotLoggedInException;
import web.param.GlobalParams;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


public class BaseController {
    private static Logger logger = LoggerFactory.getLogger(BaseController.class);
    private static final String TEST_USERID = "zhjj119@sohu.com";

    public static String redirectUrl(HttpServletRequest req, String path){
        String gp = GlobalParams.of(req).toString();

        String urlMain = ServletUtil.getUrlMain(req);
        return _.f("%s%s?%s", urlMain, path, gp);
    }

    public static User getUser(HttpServletRequest request) {
        return (User) request.getAttribute("user");
    }

    public static User getUserNotNull(HttpServletRequest req) {
        User user = getUser(req);
        if (user != null)
            return user;
        throw new NotLoggedInException();
    }

    public static String getUserid(HttpServletRequest req) {
        User user = getUser(req);
        return user == null ? null : user.userid;
    }

    public static String getUseridNotNull(HttpServletRequest req) {
        User user = getUserNotNull(req);
        return user.userid;
    }

}
