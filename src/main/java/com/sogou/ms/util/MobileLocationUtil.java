package com.sogou.ms.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jarod on 2015/1/30.
 */
public class MobileLocationUtil {
    private static Map<String, String> mobileLocationMap = new HashMap();
    private static final String resourceFileName = "src/main/resources/code_mobile.mobile";
    private static final String resourceClassPath = "code_mobile.mobile";

    static {
        File file = new File(resourceFileName);
        List<String> contentList = null;
        if (file.exists())
            contentList = ResourceUtil.fileAsStringList(resourceFileName, _.charsetUtf8);
        else
            contentList = ResourceUtil.classpathAsStringList(resourceClassPath, _.charsetUtf8);
        if (contentList != null && !contentList.isEmpty()) {
            for (String line : contentList) {
                if (_.isEmpty(line))
                    continue;
                String[] p = line.split("\\s+");
                if (p != null && p.length > 1) {
                    mobileLocationMap.put(p[0], p[1]);
                }
            }
        }
    }

    public static String getProvinceByMobile(String mobile) {
        if (_.isEmpty(mobile))
            return null;
        if (mobile.length() < 7)
            return null;
        return mobileLocationMap.get(mobile.substring(0, 7));
    }

    public static void main(String[] args) {
        System.out.println(getProvinceByMobile("13810236336"));
    }
}
