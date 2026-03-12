package com.abc.jedis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

public final class JedisClusterUtil {
    private static volatile JedisCluster primaryCluster;
    private static volatile JedisCluster secondaryCluster;
    private static JedisCluster current;

    private JedisClusterUtil() {
    }

    static {
        Set<HostAndPort> s1 = new HashSet<>();
        Set<HostAndPort> s2 = new HashSet<>();
        HostAndPort node1 = new HostAndPort("hadoop202", 7000);
        s1.add(node1);
        s2.add(node1);
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(100);
        init(s1, s2, jedisPoolConfig);
        current = primaryCluster;
    }

    /**
     * 初始化主备集群
     */
    public static void init(Set<HostAndPort> primaryNodes, Set<HostAndPort> secondaryNodes, JedisPoolConfig poolConfig) {
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
        return new JedisCluster(nodes,100,1,1,null);
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

    public static String get(String key) {
        try {
            return current.get(key);
        } catch (Exception e) {
            try {
                return current.get(key);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static void switchCluster(String key) {
        if (current == primaryCluster) {
            current = secondaryCluster;
        } else {
            current = primaryCluster;
        }
    }

    /**
     * JVM 关闭时调用
     */
    public static void close() {
        try {
            if (primaryCluster != null) primaryCluster.close();
        } catch (Exception ignored) {
        }
        try {
            if (secondaryCluster != null) secondaryCluster.close();
        } catch (Exception ignored) {
        }
    }
}