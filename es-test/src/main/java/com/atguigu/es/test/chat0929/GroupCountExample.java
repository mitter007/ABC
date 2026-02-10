package com.atguigu.es.test.chat0929;

import java.util.*;
import java.util.stream.Collectors;

public class GroupCountExample {
    public static void main(String[] args) {
        List<BaseEntity> list = Arrays.asList(
            new BaseEntity("1", "A", "D1", "xxx"),
            new BaseEntity("1", "A", "D2", "yyy"),
            new BaseEntity("1", "A", "D2", "zzz"), // D2 出现了两次
            new BaseEntity("1", "B", "D3", "aaa"),
            new BaseEntity("2", "A", "D1", "bbb"),
            new BaseEntity("2", "A", "D4", "ccc")
        );

        // 分组统计（不去重，dimcode 直接计数）
        Map<String, Long> result = list.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getSyscode() + "_" + e.getGroupCode(),
                        Collectors.counting()
                ));

        result.forEach((k, v) -> System.out.println(k + " => " + v));
    }
}
