package com.abc.job.performancelogjob.chat_1;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DualActiveRedisMapFunction
        extends RichMapFunction<Input, Output> {
    private transient volatile Config DEFAULT_CONFIG;
    private transient ConcurrentHashMap<String, Config> localCache;
    private transient DualActiveRedisHelper redisHelper;

    @Override
    public void open(Configuration parameters) throws Exception {
        localCache = new ConcurrentHashMap<>();

        // 初始化 helper
        JedisCluster primary = JedisClusterUtil.getPrimaryJedisCluster();
        JedisCluster secondary = JedisClusterUtil.getSecondaryJedisCluster();
        redisHelper = new DualActiveRedisHelper(primary, secondary);

        // 启动期全量加载配置
        loadInitialConfig();
    }

    private void loadInitialConfig() {
        String raw = redisHelper.get("config:all"); // 假设你 Redis 全量配置是 JSON 或 Map 序列化
        if (raw != null) {
            Map<String, String> all = parseAll(raw);
            all.forEach((k, v) -> localCache.put(k, parse(v)));
        }
        // 启动期也可以根据业务决定是否 fail fast
    }

    @Override
    public Output map(Input value) {
        String key = extractKey(value);

        // 1️⃣ 本地缓存命中
        Config cfg = localCache.get(key);
        if (cfg != null) {
            return process(value, cfg);
        }

        // 2️⃣ 本地缓存 miss → 从 Redis helper 获取
        String raw = redisHelper.get(key);
        if (raw != null) {
            cfg = parse(raw);
            localCache.put(key, cfg);
            return process(value, cfg);
        }

        // 3️⃣ 两个集群都不可用 → 使用默认配置
        return process(value, DEFAULT_CONFIG);
    }

    private String extractKey(Input value) {
        return value.getKey();
    }

    private Config parse(String raw) {
        return Config.fromJson(raw);
    }

    private Map<String, String> parseAll(String raw) {
        // 全量配置解析方法
        return Config.parseAllFromJson(raw);
    }

    private Output process(Input value, Config cfg) {
        return new Output(value, cfg);
    }
}
