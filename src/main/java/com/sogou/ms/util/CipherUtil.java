package com.sogou.ms.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Jarod on 2014/11/24.
 */
public class CipherUtil {
    private static final String Algorithm3Des = "DESede";

    public static String keyWith3Des(String keySeed) {
        return Md5Util.md5AsUpperHex(_.trimToEmpty(keySeed)).substring(0, 24);
    }

    public static byte[] encryptWith3Des(String key, String content) {
        if (_.isEmpty(key) || _.isEmpty(content))
            return null;

        byte[] keyByte = key.getBytes(_.charsetUtf8);
        //tracy(2014-11-27): 改为GBK编码
        byte[] contentByte = content.getBytes(_.charsetGbk);

        try {
            SecretKey desKey = new SecretKeySpec(keyByte, Algorithm3Des);
            Cipher cipher = Cipher.getInstance(Algorithm3Des);
            cipher.init(Cipher.ENCRYPT_MODE, desKey);

            return cipher.doFinal(contentByte);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptWith3Des(String key, byte[] contentByte) {
        if (_.isEmpty(key) || contentByte == null)
            return "";

        byte[] keyByte = key.getBytes(_.charsetUtf8);

        try {
            SecretKey desKey = new SecretKeySpec(keyByte, Algorithm3Des);
            Cipher cipher = Cipher.getInstance(Algorithm3Des);
            cipher.init(Cipher.DECRYPT_MODE, desKey);
            //tracy(2014-11-27): 改为GBK编码
            //return new String(cipher.doFinal(contentByte), _.charsetUtf8);
            return new String(cipher.doFinal(contentByte), _.charsetGbk);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return "";
    }
}
