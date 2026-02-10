package com.abc.job.performancelogjob.chat_1;

import redis.clients.jedis.JedisCluster;

public class DualActiveRedisHelper {

    private final JedisCluster primary;
    private final JedisCluster secondary;
    private JedisCluster current;
    private boolean primaryAvailable = true;
    private boolean secondaryAvailable = true;

    public DualActiveRedisHelper(JedisCluster primary, JedisCluster secondary) {
        this.primary = primary;
        this.secondary = secondary;
        this.current = primary;
    }

    /**
     * 获取 Redis 值（带双活切换 + 异常降级）
     */
    public String get(String key) {
        try {
            return current.get(key);
        } catch (Exception e) {
            // 切换集群
            switchClusterOnError(e);
            try {
                return current.get(key);
            } catch (Exception ignored) {
                // 两个集群都不可用
                return null;
            }
        }
    }

    private void switchClusterOnError(Exception e) {
        if (current == primary) {
            primaryAvailable = false;
            if (secondaryAvailable) current = secondary;
        } else {
            secondaryAvailable = false;
            if (primaryAvailable) current = primary;
        }
        System.out.println("Redis access failed, switched cluster. Exception: " + e.getMessage());
    }

    public boolean isAnyAvailable() {
        return primaryAvailable || secondaryAvailable;
    }
}
