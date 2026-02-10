package com.abc.job.performancelogjob;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DualRedisConfigMapFunction
        extends RichMapFunction<Input, Output> {

    private transient ConcurrentHashMap<String, Config> localCache;
    private transient volatile long lastCheckTime;
    private static final long CHECK_INTERVAL_MS = 30_000L;
    private transient volatile Config DEFAULT_CONFIG;

    @Override
    public void open(Configuration parameters) throws Exception {
        localCache = new ConcurrentHashMap<>();
        lastCheckTime = 0L;

        // 启动期：加载双活 Redis 配置
        JedisCluster jedis = null;
        try {
            jedis = DualJedisClusterUtil.getCluster();
            Map<String, String> all = jedis.hgetAll("config:all");
            if (all == null || all.isEmpty()) {
                throw new RuntimeException("No config found in Redis");
            }
            all.forEach((k, v) -> localCache.put(k, parse(v)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config from Redis at startup", e);
        }
    }

    @Override
    public Output map(Input value) {

        String key = extractKey(value);
        Config cfg = localCache.get(key);
        if (cfg != null) {
            return process(value, cfg);
        }

        // 尝试从 Redis 补偿
        long now = System.currentTimeMillis();
        if (now - lastCheckTime > CHECK_INTERVAL_MS) {
            lastCheckTime = now;
            DualJedisClusterUtil.checkAndSwitch();
        }

        try {
            JedisCluster jedis = DualJedisClusterUtil.getCluster();
            String raw = jedis.get(key);
            if (raw != null) {
                Config parsed = parse(raw);
                localCache.put(key, parsed);
                return process(value, parsed);
            }
        } catch (Exception e) {
            // 忽略异常，降级
        }

        // 最终兜底
        return process(value, DEFAULT_CONFIG);
    }

    private String extractKey(Input value) {
        return value.getKey();
    }

    private Config parse(String raw) {
        return Config.fromJson(raw);
    }

    private Output process(Input value, Config cfg) {
        return new Output(value, cfg);
    }
}
