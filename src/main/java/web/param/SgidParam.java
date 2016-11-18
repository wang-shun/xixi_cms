package web.param;

import javax.servlet.http.HttpServletRequest;

import static com.sogou.ms.util._.isEmpty;

public class SgidParam extends Param {

    public static final String KEY = "sgid";

    private String val;

    public static SgidParam of(HttpServletRequest request) {
        SgidParam param = new SgidParam();
        String sgid = ParamUtils.getSgid(request);
        if (isEmpty(sgid)) {
            sgid = "0";
        }
        param.val = sgid;
        return param;
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public String value() {
        return val;
    }

    @Override
    public String toString() {
        return key() + "=" + value();
    }

    public void setValue(String value) {
        this.val = value;
    }
}
