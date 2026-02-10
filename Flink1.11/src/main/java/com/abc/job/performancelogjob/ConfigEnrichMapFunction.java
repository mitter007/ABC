package com.abc.job.performancelogjob;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigEnrichMapFunction
        extends RichMapFunction<Input, Output> {


    // ====== 本地配置缓存（核心） ======
    private transient ConcurrentHashMap<String, Config> localCache;

    // ====== Redis 客户端 ======
    private transient JedisCluster jedisCluster;

    // ====== 熔断控制 ======
    private transient volatile boolean redisAvailable;
    private transient volatile long lastCheckTime;
    private transient volatile Config DEFAULT_CONFIG;

    // ====== 参数 ======
    private static final long REDIS_RETRY_INTERVAL_MS = 30_000L;

    @Override
    public void open(Configuration parameters) throws Exception {

        this.localCache = new ConcurrentHashMap<>();
        this.redisAvailable = true;
        this.lastCheckTime = 0L;

        // 1️⃣ 初始化 Redis
        this.jedisCluster = JedisClusterUtil.getJedisCluster();

        // 2️⃣ 启动期：从 Redis 全量加载配置
        Map<String, String> allConfigs;
        try {
            allConfigs = jedisCluster.hgetAll("config:all");
        } catch (Exception e) {
            // 启动期失败直接 fail fast
            throw new RuntimeException("Failed to load config from redis at startup", e);
        }

        if (allConfigs == null || allConfigs.isEmpty()) {
            throw new RuntimeException("Config empty, job cannot start");
        }

        // 3️⃣ 写入本地缓存
        for (Map.Entry<String, String> entry : allConfigs.entrySet()) {
            localCache.put(entry.getKey(), parse(entry.getValue()));
        }
    }

    @Override
    public Output map(Input value) {

        String key = extractKey(value);

        // ====== 1️⃣ 本地缓存命中 ======
        Config cfg = localCache.get(key);
        if (cfg != null) {
            return process(value, cfg);
        }

        // ====== 2️⃣ Redis 已熔断，直接降级 ======
        if (!redisAvailable) {
            tryRecoverRedis();
            return process(value, DEFAULT_CONFIG);
        }

        // ====== 3️⃣ Redis 补偿路径 ======
        try {
            String raw = jedisCluster.get(key);
            if (raw != null) {
                Config parsed = parse(raw);
                localCache.put(key, parsed);
                return process(value, parsed);
            }
        } catch (Exception e) {
            // 🔥 熔断
            redisAvailable = false;
        }

        // ====== 4️⃣ 最终兜底 ======
        return process(value, DEFAULT_CONFIG);
    }

    /**
     * Redis 熔断后的定期探活
     */
    private void tryRecoverRedis() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < REDIS_RETRY_INTERVAL_MS) {
            return;
        }

        lastCheckTime = now;

        try {
            jedisCluster.get("health_check_key");
            redisAvailable = true;
        } catch (Exception ignore) {
            // 仍然不可用，继续熔断
        }
    }

    @Override
    public void close() {
        // JedisCluster 是单例，这里一般不 close
    }

    // ================== 你需要实现的业务方法 ==================

    private String extractKey(Input value) {
        // 从数据中提取配置 key
        return value.getKey();
    }

    private Config parse(String raw) {
        // 反序列化配置
        return Config.fromJson(raw);
    }

    private Output process(Input value, Config cfg) {
        // 使用配置进行处理
        return new Output(value, cfg);
    }
}

class Input {
    String getKey() {
        return null;
    }

    ;

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Output {
    Input v;
    Config d;

}

class Config {
    static Config fromJson(String v) {
        return new Config();
    }

}