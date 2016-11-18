package com.sogou.ms.util.redis;

import com.sogou.ms.util.redis.hash.ModuloHash;
import redis.clients.util.Hashing;

/**
 * Created by Jarod on 2016/1/14.
 */
public interface RedisHash extends Hashing {
    public static ModuloHash MODULO_HASH = new ModuloHash();
}
