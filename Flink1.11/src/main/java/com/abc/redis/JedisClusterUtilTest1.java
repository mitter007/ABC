package com.abc.redis;

import redis.clients.jedis.JedisCluster;

import java.io.IOException;

/**
 * ClassName: JedisClusterUtilTest
 * Package: com.abc.redis
 * Description:
 *
 * @Author JWT
 * @Create 2026/2/9 15:36
 * @Version 1.0
 */
public class JedisClusterUtilTest1 {
    public static void main(String[] args) throws IOException {
        String s = get();
        System.out.println(s);
//        JedisClusterUtil.test();
    }

    static String get() {
        JedisCluster jedisCluster = JedisClusterUtil1.getPrimaryJedisCluster();


        String s = JedisClusterUtil1.getPrimaryJedisCluster().get("a");
        return s;
    }

}
