package com.sogou.ms.util.redis;

import com.sogou.ms.util._;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jarod on 2016/1/14.
 */
public class Redis extends RedisPool {
    /**
     * ******************** Redis Strings **********************
     */
    public String set(final String key, final String value) {
        return run(key, new RedisActionWithReturn<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.set(key, value);
            }
        });
    }

    public String set(final String key, final String value, final String nxxx, final String expx, final long time) {
        return run(key, new RedisActionWithReturn<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.set(key, value, nxxx, expx, time);
            }
        });
    }

    public String get(final String key) {
        return run(key, new RedisActionWithReturn<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }

    public Long setnx(final String key, final String value) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.setnx(key, value);
            }
        });
    }

    public String setex(final String key, final int seconds, final String value) {
        return run(key, new RedisActionWithReturn<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.setex(key, seconds, value);
            }
        });
    }

    public Long incr(final String key) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.incr(key);
            }
        });
    }

    public Long incrBy(final String key,final Long integer) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.incrBy(key,integer);
            }
        });
    }

    public Long decrBy(final String key,final Long integer) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.decrBy(key,integer);
            }
        });
    }

    /**
     * ******************** Redis Sets **********************
     */
    public Long sadd(final String key, final String... member) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.sadd(key, member);
            }
        });
    }

    public Long scard(final String key) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.scard(key);
            }
        });
    }

    public Boolean sismember(final String key, final String member) {
        return run(key, new RedisActionWithReturn<Boolean>() {
            @Override
            public Boolean action(Jedis jedis) {
                return jedis.sismember(key, member);
            }
        });
    }

    public Set<String> smembers(final String key) {
        return run(key, new RedisActionWithReturn<Set<String>>() {
            @Override
            public Set<String> action(Jedis jedis) {
                return jedis.smembers(key);
            }
        });
    }

    public Long srem(final String key, final String... members) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.srem(key, members);
            }
        });
    }

    /**
     * ******************** Redis Sorted Sets **********************
     */
    public Long zadd(final String key, final double score, final String member) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.zadd(key, score, member);
            }
        });
    }

    public Long zadd(final String key, final Map<String, Double> scoreMembers) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.zadd(key, scoreMembers);
            }
        });
    }

    public Long zcard(final String key) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.zcard(key);
            }
        });
    }

    public Double zscore(final String key, final String member) {
        return run(key, new RedisActionWithReturn<Double>() {
            @Override
            public Double action(Jedis jedis) {
                return jedis.zscore(key, member);
            }
        });
    }

    public Set<String> zrange(final String key, final long start, final long end) {
        return run(key, new RedisActionWithReturn<Set<String>>() {
            @Override
            public Set<String> action(Jedis jedis) {
                return jedis.zrange(key, start, end);
            }
        });
    }

    public Long zrem(final String key, final String... members) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.zrem(key, members);
            }
        });
    }

    public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long end) {
        return run(key, new RedisActionWithReturn<Set<Tuple>>() {
            @Override
            public Set<Tuple> action(Jedis jedis) {
                return jedis.zrevrangeWithScores(key, start, end);
            }
        });
    }

    public Set<Tuple> zrangeWithScores(final String key, final long start, final long end) {
        return run(key, new RedisActionWithReturn<Set<Tuple>>() {
            @Override
            public Set<Tuple> action(Jedis jedis) {
                return jedis.zrangeWithScores(key, start, end);
            }
        });
    }

    public Long zremrangeByRank(final String key, final long start, final long end) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.zremrangeByRank(key, start, end);
            }
        });
    }

    /**
     * ******************** Redis Hashes **********************
     */
    public Long hset(final String key, final String field, final String value) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.hset(key, field, value);
            }
        });
    }

    public Long hsetnx(final String key, final String field, final String value) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.hsetnx(key, field, value);
            }
        });
    }

    public Long hincrBy(final String key, final String field, final long value) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.hincrBy(key, field, value);
            }
        });
    }


    public String hmset(final String key, final Map<String, String> hash) {
        return run(key, new RedisActionWithReturn<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hmset(key, hash);
            }
        });
    }

    public Set<String> hkeys(final String key) {
        return run(key, new RedisActionWithReturn<Set<String>>() {
            @Override
            public Set<String> action(Jedis jedis) {
                return jedis.hkeys(key);
            }
        });
    }

    public Boolean hexists(final String key, final String field) {
        return run(key, new RedisActionWithReturn<Boolean>() {
            @Override
            public Boolean action(Jedis jedis) {
                return jedis.hexists(key, field);
            }
        });
    }

    public Long hdel(final String key, final String... fields) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.hdel(key, fields);
            }
        });
    }

    public String hget(final String key, final String field) {
        return run(key, new RedisActionWithReturn<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hget(key, field);
            }
        });
    }

    public List<String> hmget(final String key, final String... fields) {
        return run(key, new RedisActionWithReturn<List<String>>() {
            @Override
            public List<String> action(Jedis jedis) {
                return jedis.hmget(key, fields);
            }
        });
    }

    public Map<String, String> hgetAll(final String key) {
        return run(key, new RedisActionWithReturn<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }

    /**
     * ******************** Redis Comman **********************
     */

    public Long del(final String key) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.del(key);
            }
        });
    }

    public Boolean exist(final String key) {
        return run(key, new RedisActionWithReturn<Boolean>() {
            @Override
            public Boolean action(Jedis jedis) {
                return jedis.exists(key);
            }
        });
    }

    public Long expire(final String key, final int seconds) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.expire(key, seconds);
            }
        });
    }

    public Long pexpire(final String key, final long milliseconds) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.pexpire(key, milliseconds);
            }
        });
    }

    /**
     * ******************** Redis List **********************
     */
    public Long rpush(final String key, final String... valuss) {
        return run(key, new RedisActionWithReturn<Long>() {
            @Override
            public Long action(Jedis jedis) {
                return jedis.rpush(key, valuss);
            }
        });
    }

    public String blpop(final int timeout, final String key) {
        return run(key, new RedisActionWithReturn<String>() {
            @Override
            public String action(Jedis jedis) {
                List<String> values = jedis.blpop(timeout, key);
                return _.size(values) >= 2 ? values.get(1) : null;
            }
        });
    }
}
