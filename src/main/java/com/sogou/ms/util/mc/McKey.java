package com.sogou.ms.util.mc;

import com.sogou.ms.util._;
import com.sogou.ms.util.toolkit.AutoLog;
import net.spy.memcached.internal.OperationCompletionListener;
import net.spy.memcached.internal.OperationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User:Jarod
 */
public class McKey<T> {
    private static final Logger logger = LoggerFactory.getLogger(McKey.class);

    /**
     * @param name   用于AutoLog显示，请不要带有空格
     * @param mc
     * @param prefix
     * @param <T>
     * @return
     */
    public static <T> McKey<T> of(String name, Mc mc, String prefix) {
        return new McKey<>(name, mc, prefix);
    }

    private final Mc mc;
    public final String name;
    public final String prefix;
    public final AutoLog hitAutoLog;
    public final AutoLog costAutoLog;
    public final AutoLog setCostAutoLog;
    public final AutoLog setSuccAutoLog;

    private static final String hitFormat = "mc.hit.%s.%s.%s";
    private static final String costForamt = "mc.cost.%s.%s.%s";
    private static final String setCostForamt = "mc.set.cost.%s.%s.%s";
    private static final String setSuccForamt = "mc.set.succ.%s.%s.%s";

    private McKey(String name, Mc mc, String prefix) {
        this.mc = mc;
        this.name = _.trimToEmpty(name);
        this.prefix = _.trimToEmpty(prefix);
        this.hitAutoLog = AutoLog.of(_.f(hitFormat, mc.name, name, prefix));
        this.costAutoLog = AutoLog.of(_.f(costForamt, mc.name, name, prefix));
        this.setCostAutoLog = AutoLog.of(_.f(setCostForamt, mc.name, name, prefix));
        this.setSuccAutoLog = AutoLog.of(_.f(setSuccForamt, mc.name, name, prefix));
        logger.info("McKey instance: {}({})@{}", name, prefix, name);
    }

    public T get(String key) {
        final long start = System.currentTimeMillis();

        Object cache = mc.get(genKey(key));

        boolean isHit = cache != null;
        this.hitAutoLog.addHit(isHit);
        this.costAutoLog.end(start);
        return (T) cache;
    }

    public void set(String key, T val, int expSecond) {
        final long st = System.currentTimeMillis();
        OperationFuture<Boolean> future = mc.set(genKey(key), val, expSecond);
        if(future!=null){
            future.addListener(new OperationCompletionListener() {
                @Override public void onComplete(OperationFuture<?> future) throws Exception {
                    setCostAutoLog.end(st);
                    setSuccAutoLog.add(future.isCancelled()?0:1);
                }
            });
        }
    }

    public void delete(String key) {
        mc.delete(genKey(key));
    }

    private String genKey(String key) {
        return this.prefix + key;
    }
}
