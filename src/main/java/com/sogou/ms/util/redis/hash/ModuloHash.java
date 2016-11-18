package com.sogou.ms.util.redis.hash;

import com.sogou.ms.util.redis.RedisHash;
import redis.clients.util.SafeEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jarod on 2016/1/14.
 */
@Deprecated // 2016-1-14 19:54:41 不推荐使用，不保证jedis更新之后不会有问题
public class ModuloHash implements RedisHash {
    // 2016-1-14 17:17:42 这些Pattern要参考redis.clients.util.Sharded
    private static final Pattern jedisShardPattern_withoutName = Pattern.compile("^SHARD-(\\d)-NODE-\\d+$");
    private static final Pattern jedisShardPattern_withName = Pattern.compile("^(.*)\\*\\d+$");

    private Map<String, Integer> shardMap = new HashMap<>();

    @Override
    public long hash(String key) {
        String shardName = null;
        Matcher matcher = jedisShardPattern_withoutName.matcher(key);
        if (matcher.find())
            shardName = matcher.group(1);
        else {
            matcher = jedisShardPattern_withName.matcher(key);
            if (matcher.find())
                shardName = matcher.group(1);
        }

        if (!shardMap.containsKey(shardName))
            shardMap.put(shardName, shardMap.size());

        return shardMap.get(shardName);
    }

    @Override
    public long hash(byte[] key) {
        /* 参考redis.clients.util.Sharded，因为传进来的key实际上是jedis编码之后的
            public S getShardInfo(String key) {
                return getShardInfo(SafeEncoder.encode(getKeyTag(key)));
            }
         */
        String keyStr = SafeEncoder.encode(key);

        // 2016-1-14 19:52:14 这是兼容之前的abs项目、书架项目的逻辑
        return keyStr.hashCode() & 0x7fffffff % shardMap.size();
    }

    public long hashKey(String key) {
        return hash(SafeEncoder.encode(key));
    }
}
