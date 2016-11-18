package com.sogou.ms.util.db.kv;

import com.sogou.ms.util._;

/**
 * Created by jarod on 14-2-26.
 */
public class K {
    public static K of(String key, String defValue) {
        return new K(key, defValue);
    }

    public final String key;
    public final String defValue;

    private K(String key, String defValue) {
        this.key = key;
        this.defValue = _.trimToEmpty(defValue);
    }
}
