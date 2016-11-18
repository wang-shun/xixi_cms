package com.sogou.ms.util.db.kv;

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
public class Kv {
    private static Logger logger = LoggerFactory.getLogger(Kv.class);

    public static Kv of(DBRunner db, String table, String columnK, String columnV, int intervalSecond) {
        String name = String.format("%s.%s", db.name, table);
        table = Config.of(name + ".kv.t", table).get();
        columnK = Config.of(name + ".kv.k", columnK).get();
        columnV = Config.of(name + ".kv.v", columnV).get();
        intervalSecond = _.toInt(Config.of(name + ".kv.interval", _.toStr(intervalSecond)).get());
        return new Kv(db, table, columnK, columnV, intervalSecond);
    }

    private String name;
    private DBRunner db;
    private String sql;
    private int intervalSecond;

    private Kv(DBRunner db, String table, String columnK, String columnV, int intervalSecond) {
        this.name = String.format("%s(%s, %s)@%s", table, columnK, columnV, db.name);
        this.db = db;
        this.sql = String.format("select `%s`, `%s` from %s;", columnK, columnV, table);
        this.intervalSecond = intervalSecond;
        logger.info("kv instance: {}", this.name);
        refresh();
    }

    private void refresh() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    load();
                    try {
                        Thread.sleep(intervalSecond * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private Map<String, String> kvMap = new HashMap<String, String>();

    private void load() {
        Map<String, String> newKvMap = null;
        long start = System.currentTimeMillis();
        try {
            newKvMap = this.db.query(this.sql, new ResultSetHandler<Map<String, String>>() {
                @Override
                public Map<String, String> handle(ResultSet resultSet) throws SQLException {
                    Map<String, String> tempKvMap = new HashMap<String, String>();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            String k = resultSet.getString(1);
                            String v = resultSet.getString(2);
                            if (_.isNotEmpty(k) && _.isNotEmpty(v)) {
                                tempKvMap.put(k, v);
                            }
                        }
                    }
                    return tempKvMap;
                }
            });
        } catch (SQLException ex) {
            newKvMap = new HashMap<String, String>(0);
            logger.error("kv load error: {}", ex.getMessage());
            ex.printStackTrace();
        }

        logger.debug("kv load: {}, cost={}", this.name, System.currentTimeMillis() - start);

        Map<String, String> oldKvMap = this.kvMap;
        this.kvMap = newKvMap;

        triggleListener(oldKvMap, newKvMap);
    }

    private void triggleListener(Map<String, String> oldKvMap, Map<String, String> newKvMap) {
        Iterator<K> kIterator = kListMap.keySet().iterator();
        while (kIterator.hasNext()) {
            K k = kIterator.next();
            String oldValue = get(oldKvMap, k);
            String newValue = get(newKvMap, k);
            if (!oldValue.equals(newValue)) {
                logger.info("kv update: {}, {}={}({})", this.name, k.key, newValue, oldValue);
                List<KListener> kListenerList = kListMap.get(k);
                for (int i = 0; i < kListenerList.size(); i++) {
                    kListenerList.get(i).change(k, oldValue, newValue);
                }
            }
        }
    }

    private Map<K, List<KListener>> kListMap = Collections.synchronizedMap(new HashMap<K, List<KListener>>());

    public void addListener(K k, KListener kListener) {
        List<KListener> kListenerList = kListMap.get(k);
        if (kListenerList == null) {
            kListenerList = Collections.synchronizedList(new LinkedList<KListener>());
            kListMap.put(k, kListenerList);
        }
        kListenerList.add(kListener);
    }

    public boolean contains(String k) {
        return _.isNotEmpty(k) && this.kvMap.containsKey(k);
    }

    public String get(String k, String defValue) {
        return (_.isEmpty(k) || !contains(k)) ? defValue : this.kvMap.get(k);
    }

    public String get(K k) {
        return get(this.kvMap, k);
    }

    private static String get(Map<String, String> kvMap, K k) {
        return get(kvMap, k.key, k.defValue);
    }

    private static String get(Map<String, String> kvMap, String k, String defValue) {
        if (kvMap.containsKey(k)) {
            return kvMap.get(k);
        } else {
            String value = kvMap.get(k);
            return value == null ? defValue : value;
        }
    }

}
