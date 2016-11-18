package com.sogou.ms.util.mc;

import com.sogou.ms.util._;
import com.sogou.ms.util.infrastructure.Config;
import net.spy.memcached.internal.OperationFuture;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User:Jarod
 */
public class Mc {
    public static Mc of(String name, String server) {
        return of(name, server, "");
    }

    public static Mc of(String name, String server, String backupServer) {
        if (mcMap.containsKey(name)) {
            return mcMap.get(name);
        } else {
            return new Mc(name, server, backupServer);
        }
    }

    public final String name;
    private final McClient client;
    private final McClient backupClient;

    private Mc(String name, String server, String backupServer) {
        this.name = name;
        this.client = initClient(name, server);
        this.backupClient = initClient(_.f("%s.backup", name), backupServer);
    }

    private static final int defaultTimeout = 300;

    private static McClient initClient(String name, String server) {
        String configServers = Config.of(name + ".mc", server).getShardsForMemcached();
        if (_.isEmpty(configServers))
            return null;
        int timeout = _.toInt(Config.of(name + ".mc.timeout", null).get(), defaultTimeout);
        return new McClient(name, configServers, timeout);
    }

    public Object get(String key) {
        Object o = this.client.rawGet(key);
        if (o == null && this.backupClient != null)
            o = this.backupClient.rawGet(key);
        return o;
    }

    public OperationFuture<Boolean> set(String key, Object val, int expSecond) {
        OperationFuture<Boolean> future = this.client.rawSet(key, val, expSecond);
        if (this.backupClient != null)
            this.backupClient.rawSet(key, val, expSecond);
        return future;
    }

    public void delete(String key) {
        this.client.rawDelete(key);
        if (this.backupClient != null)
            this.backupClient.rawDelete(key);
    }

    private static Map<String, Mc> mcMap = Collections.synchronizedMap(new HashMap<String, Mc>());
}
