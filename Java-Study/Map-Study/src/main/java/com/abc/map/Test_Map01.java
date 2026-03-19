package com.abc.map;

/**
 * ClassName: Test_Map01
 * Package: com.abc.map
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/19 20:32
 * @Version 1.0
 */
public class Test_Map01 {
    public static void main(String[] args) {

        MyMap<String, Integer> map = new MyMap<>();
        map.put("a",1);
        map.put("b",1);
        map.put("c",1);
        System.out.println(map.get("a"));
        System.out.println(map.get("b"));
        System.out.println(map.get("c"));

    }
}
