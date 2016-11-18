package com.sogou.ms.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Jarod on 2015/1/30.
 */
public class IpLocationUtil {
    public static List<Triple<Long, Long, String>> ipProvinceList = new ArrayList();

    static {
        String filename = "src/main/resources/ip2location-2015.01.22.lst";
        File file = new File(filename);
        List<String> fileContent = null;
        if (file.exists()) {
            fileContent = ResourceUtil.fileAsStringList(filename, _.charsetUtf8);
        } else {
            fileContent = ResourceUtil.classpathAsStringList("ip2location-2015.01.22.lst", _.charsetUtf8);
        }

        for (String line : fileContent) {
            if (_.isEmpty(line))
                continue;

            String[] params = line.split("\\s+");
            if (params == null || params.length < 3)
                continue;

            String startIp = params[0];
            String endIp = params[1];
            String postCode = params[2];

            if (_.isEmpty(startIp) || _.isEmpty(endIp) || _.isEmpty(postCode))
                continue;

            long start = IpUtil.getIpFromString(startIp);
            long end = IpUtil.getIpFromString(endIp);
            if (start >= end)
                continue;
            ipProvinceList.add(Triple.of(start, end, postCode));
        }
        Collections.sort(ipProvinceList, new Comparator<Triple<Long, Long, String>>() {
            @Override
            public int compare(Triple<Long, Long, String> o1, Triple<Long, Long, String> o2) {
                return (int) (o1.a - o2.a);
            }
        });
    }

    public static String getPostcodeByIp(String ip) {
        long ipVal = IpUtil.getIpFromString(ip);
        int index = Collections.binarySearch(ipProvinceList, Triple.of(ipVal, ipVal, ""), new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Triple<Long, Long, String> t1 = (Triple<Long, Long, String>) o1;
                Triple<Long, Long, String> t2 = (Triple<Long, Long, String>) o2;
                if (t1.a <= t2.a && t1.b >= t2.b)
                    return 0;
                if (t1.b < t2.a)
                    return -1;
                return 1;
            }
        });

        if (index >= 0)
            return ipProvinceList.get(index).c;
        return null;

    }

    public static String getProvinceByIp(String ip) {
        return PostcodeUtil.getProvinceByPost(getPostcodeByIp(ip));
    }
}
