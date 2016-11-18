package com.sogou.ms.util;

/**
 * Created by jarod on 2014/11/25.
 */
public class Base64Util {
    public static String enc(byte[] bytes) {
        if (bytes == null)
            return "";

        /*
        其中base64的转码需要导入apache的codec包，不能使用sun的那个包。
        原因是sun的那个包会在文件过大的时候自己加入回车换行。
         */
//        return new sun.misc.BASE64Encoder().encode(bytes);
        return new org.apache.commons.codec.binary.Base64().encodeToString(bytes);
    }

    public static String encAsUtf8(String content) {
        return enc(toUtf8Bytes(content));
    }

    private static byte[] toUtf8Bytes(String content) {
        return _.trimToEmpty(content).getBytes(_.charsetUtf8);
    }

    public static String decAsUtf8(String content) {
        return new String(dec(content), _.charsetUtf8);
    }

    public static byte[] dec(String content) {
        return new org.apache.commons.codec.binary.Base64().decode(content);
    }
}
