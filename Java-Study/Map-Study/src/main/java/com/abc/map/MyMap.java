package com.abc.map;

/**
 * ClassName: MyMap
 * Package: com.abc
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/19 20:11
 * @Version 1.0
 */
public class MyMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;

    private Node<K, V>[] table;

    public MyMap() {
        this.table = new Node[DEFAULT_CAPACITY];
    }

    /**
     * 关键点：
     * <p>
     * hash：避免重复计算
     * <p>
     * next：链表指针
     *
     * @param <K>
     * @param <V>
     */
    class Node<K, V> {
        final int hash; //避免重复计算
        final K key;
        V value;
        Node<K, V> next; // 链表指针

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }


    // TODO 2 实现hash 方法

    /**
     * 👉 为什么这样做？
     * <p>
     * 防止 hashCode 分布不均
     * <p>
     * 让高位参与运算
     *
     * @param key
     * @return
     */

    private int hash(K key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16); //扰动函数（JDK同款简化）
    }
    // TODO 3 实现index方法

    /**
     * 重点理解：
     * <p>
     * 等价于 %
     * <p>
     * 但性能更高
     * <p>
     * 前提：数组长度必须是 2 的幂
     *
     * @param hash
     * @return
     */
    private int index(int hash) {

        return (table.length - 1) & hash;
    }

    // TODO 4 实现put方法
    public void put(K key, V value) {
        int hash = hash(key);
        int index = index(hash);
        Node<K, V> head = table[index];

        //1.如果
        if (head == null) {
            table[index] = new Node<>(hash, key, value, null);
            return;
        }
        Node<K, V> current = head;
        while (true) {
            // key 已存在 ->覆盖
            if (current.hash == hash && (current.key == key || current.key.equals(key))) {
                current.value = value;
                return;
            }
            if (current.next == null) {
                current.next = new Node<>(hash, key, value, null);
            }
            // 赋值，重新循环一遍
            current = current.next;
        }

    }

    public V get(K key) {
        int hash = hash(key);
        int index = index(hash);
        Node<K, V> current = table[index];
        while (current != null) {
            if (current.hash == hash && (current.key == key || current.key.equals(key))) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }


}
