package com.abc.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

public final class JedisClusterUtil {

    private static volatile JedisCluster primaryCluster;
    private static volatile JedisCluster secondaryCluster;
    private static volatile JedisCluster current;

    private static Set<HostAndPort> primaryNodes;
    private static Set<HostAndPort> secondaryNodes;
    private static JedisPoolConfig poolConfig;

    private JedisClusterUtil() {}

    static {
        primaryNodes = new HashSet<>();
        secondaryNodes = new HashSet<>();

        HostAndPort node = new HostAndPort("hadoop202", 7000);
        primaryNodes.add(node);
        secondaryNodes.add(node);

        poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(100);

        // ⚠️ 只初始化主集群
        primaryCluster = createJedisCluster(primaryNodes, poolConfig);
        current = primaryCluster;

        // JVM 关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(JedisClusterUtil::close));
    }

    private static JedisCluster createJedisCluster(Set<HostAndPort> nodes,
                                                   JedisPoolConfig poolConfig) {
        return new JedisCluster(
                nodes,
                300,
                300,
                2,
                poolConfig
        );
    }

    public static String get(String key) {
        try {
            return current.get(key);
        } catch (Exception e) {
            switchCluster();
            try {
                return current.get(key);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * 主备切换（线程安全）
     */
    private static void switchCluster() {
        synchronized (JedisClusterUtil.class) {

            // 二次校验，避免多线程重复切换
            JedisCluster old = current;

            if (old == primaryCluster) {
                // 切到备
                if (secondaryCluster == null) {
                    secondaryCluster = createJedisCluster(secondaryNodes, poolConfig);
                }
                current = secondaryCluster;
                closeQuietly(primaryCluster);
                primaryCluster = null;
            } else {
                // 切回主
                if (primaryCluster == null) {
                    primaryCluster = createJedisCluster(primaryNodes, poolConfig);
                }
                current = primaryCluster;
                closeQuietly(secondaryCluster);
                secondaryCluster = null;
            }
        }
    }

    private static void closeQuietly(JedisCluster cluster) {
        if (cluster != null) {
            try {
                cluster.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * JVM 关闭时调用
     */
    public static void close() {
        synchronized (JedisClusterUtil.class) {
            closeQuietly(primaryCluster);
            closeQuietly(secondaryCluster);
            primaryCluster = null;
            secondaryCluster = null;
            current = null;
        }
    }
}
