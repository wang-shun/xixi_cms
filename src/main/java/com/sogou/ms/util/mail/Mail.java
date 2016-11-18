package com.sogou.ms.util.mail;

import com.sogou.ms.util._;
import com.sogou.ms.util.crawler.Crawler;
import com.sogou.ms.util.crawler.PostEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;

/**
 * User:Jarod
 */
public class Mail {
    public enum Mode {
        html, txt
    }

    private static final String mailServer = "http://portal.sys.sogou-op.org/portal/tools/send_mail.php";

    /**
     * 发送邮件
     *
     * @param uid        申请权限的user_id，请使用sogou-inc邮箱账号
     * @param sendName   发信人姓名
     * @param sendMail   发信人email
     * @param title      邮件标题
     * @param body       邮件内容
     * @param mode       邮件类型，html或txt
     * @param attachName 附件文件名
     * @param attachBody 附件正文
     * @param maillist   收信人邮箱
     */
    public static void send(String uid, String sendName, String sendMail,
                            String title, String body, Mode mode, String attachName,
                            String attachBody, String... maillist) {
        HttpEntity entity = PostEntity.of(_.charsetGbk, "uid",
                _.trimToEmpty(uid), "fr_name", _.trimToEmpty(sendName),
                "fr_addr", _.trimToEmpty(sendMail), "title",
                _.trimToEmpty(title), "body", _.trimToEmpty(body), "mode",
                _.trimToEmpty(mode.toString()), "maillist", f(maillist),
                "attname", _.trimToEmpty(attachName), "attbody",
                _.trimToEmpty(attachBody));
        System.out.println("start to send mail...");
        Pair<Integer, String> result = mailCrawler.post(mailServer,
                entity);
        if (result != null) {
            System.out.println(result.getLeft());
            System.out.println(result.getRight());
        }
    }

    private static String f(String... maillist) {
        if (maillist != null && maillist.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < maillist.length; i++) {
                sb.append(_.trimToEmpty(maillist[i]));
                sb.append(';');
            }
            return sb.toString();
        }
        return "";
    }

    private static Crawler mailCrawler = Crawler.of("mail.crawler");
}
