package com.sogou.ms.util.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.sogou.ms.util.JsonUtil;
import com.sogou.ms.util._;
import com.sogou.ms.util.crawler.Crawler;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Jarod on 2014/12/29.
 */
public class Sms {
    private static final String sendUrl = "http://sms.sogou-op.org/portal/mobile/smsproxy.php";
    private static final Crawler sendCrawler = Crawler.of("sms.send");
    private static final Logger logger = LoggerFactory.getLogger(Sms.class);

    public static Pair<Boolean, String> send(String content, String appid, String... phoneList) {
        String url = _.f("%s?number=%s&desc=%s&appid=%s&type=json", sendUrl, formatPhone(phoneList), _.urlencGbk(content), _.urlencUtf8(appid));
        String result = sendCrawler.getTrimmedContent(url);
        try {
            JsonNode jsonNode = JsonUtil.parse(result);
            if (jsonNode != null) {
                String code = JsonUtil.text(jsonNode, "code");
                String desc = JsonUtil.text(jsonNode, "desc");

                // 2016-1-5 15:15:34 不知道什么时候返回的格式有变化：
                /* 成功的时候code是0
                {"code":0,"desc":"Sent to cellphone successfully, note it"}
                 */
                if ("00".equals(code) | "0".equals(code))
                    return Pair.of(true, result);

                logger.error("[SmsFail]url:{},code:{},desc:{}", url, code, desc);
            }
        } catch (IOException e) {
            logger.error("[SmsException]url:{},error:{}", url, e.getMessage());
        }
        return Pair.of(false, result);
    }

    private static String formatPhone(String... phoneList) {
        StringBuilder sb = new StringBuilder();
        if (phoneList != null && phoneList.length > 0) {
            for (String phone : phoneList) {
                sb.append(phone);
                sb.append(',');
            }
        }
        return _.urlencUtf8(sb.toString());
    }

    public static void main(String[] args) {
        send("试试", "sogou-ms-yuedu", "13810236336");
    }
}
