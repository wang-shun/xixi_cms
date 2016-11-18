package com.sogou.ms.util.redis;

import com.sogou.ms.util._;
import com.sogou.ms.util.infrastructure.Config;
import com.sogou.ms.util.toolkit.AutoLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jarod on 2016/1/14.
 */
public class RedisPool {
    private static final Logger logger = LoggerFactory.getLogger(Redis.class);
    private static final int DEFAULT_TIMEOUT = 2000;

    public static Redis of(String name, String server) {
        return of(name, server, null);
    }

    public static Redis of(String name, String server, String password) {
        return of(name, server, password, null);
    }

    private static final Pattern serverInfoPattern = Pattern.compile("^([^:]+):(\\d+)");

    public static Redis of(String name, String server, String password, RedisHash hash) {
        String configServers = Config.of(_.f("%s.redis", name), server).get();
        String configPassword = Config.of(_.f("%s.redis.password", name), password).get();
        int timeout = _.toInt(Config.of(name + ".redis.timeout", null).get(), DEFAULT_TIMEOUT);
        boolean testOnBorrow = "true".equals(Config.of(name + ".redis.testOnBorrow", "false").get());
        boolean testOnReturn = "true".equals(Config.of(name + ".redis.testOnReturn", "false").get());
        boolean testOnCreate = "true".equals(Config.of(name + ".redis.testOnCreate", "true").get());

        List<String> configServerInfoList = Config.parseShards(configServers);
        List<JedisShardInfo> shardInfoList = new ArrayList<>();
        for (String configServerInfo : configServerInfoList) {
            if (_.isEmpty(configServerInfo))
                continue;

            Matcher matcher = serverInfoPattern.matcher(configServerInfo);
            if (matcher.find()) {
                String host = matcher.group(1);
                int port = _.toInt(matcher.group(2));
                JedisShardInfo jedisShardInfo = new JedisShardInfo(host, port, timeout);
                if (_.nonEmpty(configPassword))
                    jedisShardInfo.setPassword(configPassword);

                shardInfoList.add(jedisShardInfo);
                logger.info("[RedisShard]{}:{}", host, port);
            }
        }

        Redis redis = new Redis();
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setTestOnCreate(testOnCreate);
        redis.name = name;
        redis.pool = hash == null ? new ShardedJedisPool(config, shardInfoList) : new ShardedJedisPool(config, shardInfoList, hash);
        redis.cost = AutoLog.of(_.f("redis.cost.%s", name));
        redis.err = AutoLog.of(_.f("redis.err.%s", name));
        redis.conn = AutoLog.of(_.f("redis.conn.%s", name));
        redis.close = AutoLog.of(_.f("redis.close.%s", name));
        redis.shard = AutoLog.of(_.f("redis.shard.%s", name));
        redis.action = AutoLog.of(_.f("redis.action.%s", name));
        logger.info("[RedisInit]{}:{}", name, configServers);
        return redis;
    }

    public String name = "someone";
    protected ShardedJedisPool pool = null;
    protected AutoLog cost = null;
    protected AutoLog err = null;
    protected AutoLog conn = null;
    protected AutoLog close = null;
    protected AutoLog shard = null;
    protected AutoLog action = null;

    /*---------------- jedis operation ----------------*/

    protected void run(String key, RedisActionWithoutReturn jedisAction) {
        final long start = System.currentTimeMillis();
        ShardedJedis resource = null;
        try {
            resource = getResource();
            final long startShard = System.currentTimeMillis();
            Jedis jedis = resource == null ? null : resource.getShard(key);
            shard.end(startShard);
            if (jedis != null) {
                final long startAction = System.currentTimeMillis();
                jedisAction.action(jedis);
                action.end(startAction);
            }
            err.add(0);
        } catch (Exception e) {
            logger.error("[JedisException]error:{}", e.getMessage());
            err.add(1);
        } finally {
            closeResourceQuietly(resource);
        }
        cost.end(start);
    }

    protected <T> T run(String key, RedisActionWithReturn<T> jedisAction) {
        final long start = System.currentTimeMillis();
        ShardedJedis resource = null;
        T result = null;
        try {
            resource = getResource();
            final long startShard = System.currentTimeMillis();
            Jedis jedis = resource == null ? null : resource.getShard(key);
            shard.end(startShard);
            if (jedis != null) {
                final long startAction = System.currentTimeMillis();
                result = jedisAction.action(jedis);
                action.end(startAction);
            }
            err.add(0);
        } catch (Exception e) {
            logger.error("[JedisException]error:{}", e.getMessage());
            err.add(1);
        } finally {
            closeResourceQuietly(resource);
        }

        cost.end(start);
        return result;
    }

    private ShardedJedis getResource() {
        final long start = System.currentTimeMillis();
        final ShardedJedis jedis = pool == null ? null : pool.getResource();
        conn.end(start);
        return jedis;
    }

    private void closeResourceQuietly(ShardedJedis jedis) {
        final long start = System.currentTimeMillis();
        if (jedis != null)
            jedis.close();
        close.end(start);
    }


    /*---------------- jedis interface ----------------*/

    protected static interface RedisActionWithoutReturn {
        void action(Jedis jedis);
    }

    protected static interface RedisActionWithReturn<T> {
        T action(Jedis jedis);
    }
}
