package com.abc.job.performancelogjob;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

public final class JedisClusterUtil {

    private static volatile JedisCluster jedisCluster;

    private JedisClusterUtil() {
    }

    public static JedisCluster getJedisCluster() {
        if (jedisCluster == null) {
            synchronized (JedisClusterUtil.class) {
                if (jedisCluster == null) {
                    jedisCluster = createJedisCluster();
                }
            }
        }
        return jedisCluster;
    }

    private static JedisCluster createJedisCluster() {

        // 1. Redis Cluster 节点
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("hadoop202", 7000));
        nodes.add(new HostAndPort("hadoop202", 7001));
        nodes.add(new HostAndPort("hadoop202", 7002));

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(2);

        JedisCluster jedisCluster = new JedisCluster(
                nodes,
                2000,   // connectionTimeout
                2000,   // soTimeout
                5,      // maxRedirections（建议 5~10）
                poolConfig
        );
        return jedisCluster;
    }

    /**
     * JVM 关闭时调用
     */
    public static void close() {
        if (jedisCluster != null) {
            try {
                jedisCluster.close();
            } catch (Exception ignored) {
            }
        }
    }


}
