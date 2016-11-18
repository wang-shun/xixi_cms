package web.param;

import com.sogou.ms.util.ServletUtil;

import javax.servlet.http.HttpServletRequest;

import static com.sogou.ms.util._.isEmpty;

public class UsidParam extends Param {

    private static final String KEY = "usid";

    public String val;

    public static UsidParam of(HttpServletRequest request) {
        UsidParam param = new UsidParam();
        String usid = request.getHeader(KEY);
        if (isEmpty(usid))
            usid = ServletUtil.getCookie(request, KEY);
        if (isEmpty(usid))
            usid = "-";
        param.val = usid;
        return param;
    }

    @Override
    public String key() {
        return "uID";
    }

    @Override
    public String value() {
        return val;
    }

    @Override
    public String toString() {
        return key() + "=" + value();
    }
}
