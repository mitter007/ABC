package com.abc.job.performancelogjob.chat_1;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;

public final class JedisClusterUtil {

    private static volatile JedisCluster primaryCluster;
    private static volatile JedisCluster secondaryCluster;

    private JedisClusterUtil() {}

    /**
     * 初始化主备集群
     */
    public static void init(Set<HostAndPort> primaryNodes,
                            Set<HostAndPort> secondaryNodes,
                            JedisPoolConfig poolConfig) {

        if (primaryCluster == null) {
            synchronized (JedisClusterUtil.class) {
                if (primaryCluster == null) {
                    primaryCluster = createJedisCluster(primaryNodes, poolConfig);
                }
            }
        }

        if (secondaryCluster == null) {
            synchronized (JedisClusterUtil.class) {
                if (secondaryCluster == null) {
                    secondaryCluster = createJedisCluster(secondaryNodes, poolConfig);
                }
            }
        }
    }

    private static JedisCluster createJedisCluster(Set<HostAndPort> nodes, JedisPoolConfig poolConfig) {
        return new JedisCluster(
                nodes,
                300,   // connectionTimeout ms
                300,   // soTimeout ms
                2,     // maxRedirections
                poolConfig
        );
    }

    public static JedisCluster getPrimaryJedisCluster() {
        if (primaryCluster == null) {
            throw new IllegalStateException("Primary Redis cluster not initialized");
        }
        return primaryCluster;
    }

    public static JedisCluster getSecondaryJedisCluster() {
        if (secondaryCluster == null) {
            throw new IllegalStateException("Secondary Redis cluster not initialized");
        }
        return secondaryCluster;
    }

    /**
     * JVM 关闭时调用
     */
    public static void close() {
        try {
            if (primaryCluster != null) primaryCluster.close();
        } catch (Exception ignored) {}
        try {
            if (secondaryCluster != null) secondaryCluster.close();
        } catch (Exception ignored) {}
    }
}
