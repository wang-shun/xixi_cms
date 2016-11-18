package com.sogou.ms.util.db.kv;

/**
 * User:Jarod
 */
public interface KListener {
    public void change(K k, String oldValue, String newValue);
}
