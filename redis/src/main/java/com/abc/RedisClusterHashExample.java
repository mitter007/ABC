package com.abc;



import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedisClusterHashExample {

    public static void main(String[] args) {
        // 1. 配置集群节点
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("hadoop202", 7000));
        nodes.add(new HostAndPort("hadoop203", 7001));
        nodes.add(new HostAndPort("hadoop204", 7002));
        // 根据你的集群节点继续添加...

        // 2. 创建 JedisCluster 对象
        try (JedisCluster jedisCluster = new JedisCluster(nodes)) {

            // 3. 准备 Hash 数据
            String key = "user:1001";
            Map<String, String> hash = new HashMap<>();
            hash.put("name", "Alice");
            hash.put("age", "30");

            // 4. 写入 Redis 集群
            jedisCluster.hset(key, hash);

            // 5. 读取验证
            Map<String, String> result = jedisCluster.hgetAll(key);
            System.out.println(""+key+": " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
