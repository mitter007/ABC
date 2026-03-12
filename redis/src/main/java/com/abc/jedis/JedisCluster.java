package com.abc.jedis;

/**
 * ClassName: JedisCluster
 * Package: com.abc.jedis
 * Description:
 *
 * @Author JWT
 * @Create 2026/2/11 10:43
 * @Version 1.0
 */

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class JedisCluster {
    try {
        String value = RedisUtil.get(key);

        if (value == null) {
            // key 不存在（正常情况）
            return;
        }

        String value2 = RedisUtil.get(value);

        if (value2 == null) {
            // 第二个 key 不存在
            return;
        }

        // 正常业务逻辑

    } catch (
    JedisConnectionException e) {
        // Redis 不可用
        log.error("Redis cluster unavailable", e);

        // 可以做：
        // 1. 切换备用集群
        // 2. 降级处理
        // 3. 直接返回失败
    }catch (
    Exception e) {
        // Redis 不可用
        log.error("异常", e);

    }


}