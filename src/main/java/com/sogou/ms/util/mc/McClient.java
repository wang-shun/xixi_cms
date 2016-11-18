package com.sogou.ms.util.mc;

import com.sogou.ms.util._;
import com.sogou.ms.util.toolkit.AutoLog;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationCompletionListener;
import net.spy.memcached.internal.OperationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Jarod on 2015/6/23.
 */
public class McClient {
    private static final Logger logger = LoggerFactory.getLogger(McClient.class);

    //    private final String name;
    private final String clientInfo;
    private final long timeout;
    private MemcachedClient client = null;
    private final AutoLog hitAutoLog;
    private final AutoLog costAutoLog;
    private final AutoLog setCostAutoLog;
    private final AutoLog setSucc;

    public McClient(String name, String server, int timeout) {
        this.clientInfo = _.f("%s@%s", name, server);
        this.timeout = timeout;
        this.hitAutoLog = AutoLog.of("mc.hit." + name);
        this.costAutoLog = AutoLog.of("mc.cost." + name);
        this.setCostAutoLog = AutoLog.of("mc.set.cost." + name);
        this.setSucc = AutoLog.of("mc.set.succ."+name);
        if (_.isEmpty(server)) {
            logger.error("mc.server is null: {}", name);
        } else {
            try {
                client = new MemcachedClient(McHelper.newConnectionFactory(), AddrUtil.getAddresses(server));
                logger.info("mc init: {}", this.clientInfo);
            } catch (Exception e) {
                logger.error("mc init error: {}, error:{}", this.clientInfo, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Object rawGet(String key) {
        long start = System.currentTimeMillis();
        Object ret = null;
        if (this.clientInfo == null) {
            logger.error("mc client is null: {}", this.clientInfo);
        } else {
            GetFuture<Object> future = null;
            try {
                future = client.asyncGet(key);
            } catch (Exception ex) {
                logger.error("mc get future error. key:{}, client:{}, error:{}", key, this.clientInfo, ex.getMessage());
            }
            if (future != null) {
                try {
                    ret = future.get(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.error("mc get error. key:{}, client:{}, error:{}", key, this.clientInfo, e.getMessage());
                } catch (TimeoutException e) {
                    logger.error("mc get error. key:{}, client:{}, error:{}", key, this.clientInfo, e.getMessage());
                } catch (ExecutionException e) {
                    logger.error("mc get error. key:{}, client:{}, error:{}", key, this.clientInfo, e.getMessage());
                } finally {
                    future.cancel(true);
                }
            }
        }

        boolean isHit = ret != null;

        logger.debug("[McClient][Get]hit={},key={},client={}", isHit, key, this.clientInfo);

        hitAutoLog.addHit(isHit);
        costAutoLog.end(start);

        return ret;
    }

    public OperationFuture<Boolean> rawSet(String key, Object val, int expSecond) {
        if (client == null) {
            logger.error("mc client is null: {}", this.clientInfo);
            return null;
        } else {
            try{
                final long st = System.currentTimeMillis();
                OperationFuture<Boolean> future = client.set(key, expSecond, val);
                future.addListener(new OperationCompletionListener() {
                    @Override public void onComplete(OperationFuture<?> future) throws Exception {
                        setCostAutoLog.end(st);
                        setSucc.add(future.isCancelled()?0:1);
                    }
                });
                return future;
            }catch (RuntimeException e){
                logger.error("mc set error. key:{}, client:{}, error:{}", key, this.clientInfo ,e.getMessage());
                throw new RuntimeException(e);
            }finally {
                logger.debug("[McClient][Set]key={},value={},exp={}", key, val, expSecond);
            }
        }
    }

    public void rawDelete(String key) {
        if (client == null) {
            logger.error("mc client is null: {}", this.clientInfo);
        } else {
            client.delete(key);
            logger.debug("[McClient][Delete]key={}", key);
        }
    }

    public void test(){
       Collection<MemcachedNode> list = client.getNodeLocator().getAll();
        Iterator<MemcachedNode> iterator = list.iterator();
        while (iterator.hasNext()){
            MemcachedNode node = iterator.next();
            System.out.println(node.toString());
        }
    }
}
