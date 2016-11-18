package com.sogou.ms.util;

import java.util.Map;

/**
 * Created by Jarod on 2015/2/10.
 */
public class MobileTypeUtil {
    private static Map<String, String> mobileTypeMap = _.asStringMap(
            "134", "1",
            "135", "1",
            "136", "1",
            "137", "1",
            "138", "1",
            "139", "1",
            "147", "1",
            "150", "1",
            "151", "1",
            "152", "1",
            "157", "1",
            "158", "1",
            "159", "1",
            "178", "1",
            "182", "1",
            "183", "1",
            "184", "1",
            "187", "1",
            "188", "1",
            "130", "2",
            "131", "2",
            "132", "2",
            "145", "2",
            "155", "2",
            "156", "2",
            "176", "2",
            "185", "2",
            "186", "2",
            "133", "3",
            "134", "3",
            "153", "3",
            "177", "3",
            "180", "3",
            "181", "3",
            "189", "3",
            "170", "8"
    );

    public static String getMobileType(String mobile) {
        if (_.nonEmpty(mobile) && mobile.length() > 3)
            return mobileTypeMap.get(mobile.substring(0, 3));
        return null;
    }
}
