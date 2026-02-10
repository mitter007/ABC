package com.abc.job.performancelogjob;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;

public final class DualJedisClusterUtil {

    private static volatile JedisCluster primaryCluster;
    private static volatile JedisCluster secondaryCluster;
    private static volatile boolean primaryAvailable = true;
    private static volatile boolean secondaryAvailable = true;

    private DualJedisClusterUtil() {}

    public static JedisCluster getCluster() {
        // 优先主集群
        if (primaryAvailable) {
            return primaryCluster;
        }
        if (secondaryAvailable) {
            return secondaryCluster;
        }
        throw new RuntimeException("Both Redis clusters are unavailable");
    }

    public static void init(Set<HostAndPort> primaryNodes,
                            Set<HostAndPort> secondaryNodes,
                            JedisPoolConfig poolConfig) {

        if (primaryCluster == null) {
            synchronized (DualJedisClusterUtil.class) {
                if (primaryCluster == null) {
                    primaryCluster = createJedisCluster(primaryNodes, poolConfig);
                }
            }
        }

        if (secondaryCluster == null) {
            synchronized (DualJedisClusterUtil.class) {
                if (secondaryCluster == null) {
                    secondaryCluster = createJedisCluster(secondaryNodes, poolConfig);
                }
            }
        }
    }

    private static JedisCluster createJedisCluster(Set<HostAndPort> nodes, JedisPoolConfig poolConfig) {
        return new JedisCluster(
                nodes,
                300,   // connectionTimeout
                300,   // soTimeout
                2,     // maxRedirections
                poolConfig
        );
    }

    public static void checkAndSwitch() {
        try {
            primaryCluster.get("health_check_key");
            primaryAvailable = true;
        } catch (Exception e) {
            primaryAvailable = false;
        }

        try {
            secondaryCluster.get("health_check_key");
            secondaryAvailable = true;
        } catch (Exception e) {
            secondaryAvailable = false;
        }
    }
}
