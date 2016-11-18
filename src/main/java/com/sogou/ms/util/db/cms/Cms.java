package com.sogou.ms.util.db.cms;

import com.sogou.ms.util._;
import com.sogou.ms.util.db.DBRunner;
import com.sogou.ms.util.infrastructure.Config;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User:Jarod
 */
public class Cms {
    private static final Logger logger = LoggerFactory.getLogger(Cms.class);

    private String name;
    private DBRunner db;
    private String sql;
    private long interval;
    private CmsListener cmsListener;

    public static Cms of(String name, int intervalSecond, DBRunner db, String sql, CmsListener cmsListener) {
        return new Cms(name, intervalSecond, db, sql, cmsListener);
    }

    private Cms(String name, int intervalSecond, DBRunner db, String sql, CmsListener cmsListener) {
        this.name = String.format("%s(%ss)@%s", name, intervalSecond, db.name);
        this.db = db;
        this.sql = sql;
        this.interval = _.toInt(Config.of(name + ".interval", _.toStr(intervalSecond)).get(), intervalSecond) * 1000;
        this.cmsListener = cmsListener;
        logger.info("cms instance: {}", this.name);

        // 初始数据加载
        load();

        // 开始刷新
        refresh();
    }

    private void refresh() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    load();
                }
            }
        });
        thread.start();
    }

    List<Map<String, String>> cmsList = Collections.synchronizedList(new ArrayList<Map<String, String>>());

    private void load() {
        List<Map<String, String>> newCmsList = null;
        long start = System.currentTimeMillis();

        try {
            newCmsList = db.query(sql, new ResultSetHandler<List<Map<String, String>>>() {
                @Override
                public List<Map<String, String>> handle(ResultSet rs) throws SQLException {
                    if (rs != null) {
                        List<Map<String, String>> tmpCmsList = new ArrayList<Map<String, String>>();
                        int count = rs.getMetaData().getColumnCount();
                        while (rs.next()) {
                            Map<String, String> newCmsMap = new HashMap<String, String>();
                            for (int i = 1; i <= count; i++) {
                                newCmsMap.put(_.toStr(i), rs.getString(i));
                                newCmsMap.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                            }
                            tmpCmsList.add(newCmsMap);
                        }
                        return tmpCmsList;
                    }
                    return Collections.emptyList();
                }
            });

        } catch (SQLException e) {
            logger.error("cms load error: {}, error:{}", this.name, e.getMessage());
            e.printStackTrace();
            //tracy(2015-2-27): SQL异常,直接返回
            return;
        }

        logger.debug("cmd load: {}, cost={}", this.name, System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        if (diff(this.cmsList, newCmsList)) {
            this.cmsListener.change(newCmsList);
            this.cmsList = newCmsList;
            logger.info("cmd update: {}, cost={}", this.name, System.currentTimeMillis() - start);
        }
    }

    private static boolean diff(List<Map<String, String>> oldData,
                                List<Map<String, String>> newData) {
        if (oldData == null && newData == null)
            return false;

        if (oldData == null || newData == null)
            return true;

        if (oldData.size() != newData.size())
            return true;

        for (int i = 0; i < oldData.size(); i++) {
            Map<String, String> oldMap = oldData.get(i);
            Map<String, String> newMap = newData.get(i);
            if (oldMap.size() != newMap.size())
                return true;
            for (Iterator<String> it = oldMap.keySet().iterator(); it.hasNext(); ) {
                String oldKey = it.next();
                if (!newMap.containsKey(oldKey))
                    return true;

                String oldValue = oldMap.get(oldKey);
                String newValue = newMap.get(oldKey);

                if (!_.trimToEmpty(oldValue).equals(_.trimToEmpty(newValue))) {
                    return true;
                }
            }
        }
        return false;
    }
}
