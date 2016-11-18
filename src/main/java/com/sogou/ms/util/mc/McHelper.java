package com.sogou.ms.util.mc;

import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import java.util.concurrent.TimeUnit;

/**
 * User:Jarod
 */
public class McHelper {
    public static long defaultOpQueueMaxBlockTime = TimeUnit.SECONDS.toMillis(1); // 等待加入到发送队列的最长等待时间，默认10s
    public static long defaultOperationTimeout = 100; // 超过此时间则不再重试其他节点，默认2.5s
    public static long defaultMaxReconnectDelay = 120; // 某节点挂掉后，过多长时间尝试重连，默认30s
    public static int defaultMaxTimeoutexceptionThreshold = 50; // 多少次超时后，认为这个节点已经挂掉，默认998
    public static int defaultMaxSize = 1024 * 1024; // 1M

    public static BinaryConnectionFactory newConnectionFactory() {
        return new BinaryConnectionFactory() {
            public boolean isDaemon() {
                return true;
            }

            public long getOpQueueMaxBlockTime() {
                return defaultOpQueueMaxBlockTime;
            }

            public long getOperationTimeout() {
                return defaultOperationTimeout;
            }

            public long getMaxReconnectDelay() {
                return defaultMaxReconnectDelay;
            }

            public int getTimeoutExceptionThreshold() {
                return defaultMaxTimeoutexceptionThreshold;
            }

            public Transcoder<Object> getDefaultTranscoder() {
                SerializingTranscoder transcoder = new SerializingTranscoder(defaultMaxSize);
                transcoder.setCompressionThreshold(Integer.MAX_VALUE);
                return transcoder;
            }
        };
    }
}
