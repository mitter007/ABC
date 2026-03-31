package com.abc.map;

class MyHashMap<K, V> {

    static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private Node<K, V>[] table;
    private int size;
    private int threshold;

    public MyHashMap() {
        table = new Node[DEFAULT_CAPACITY];
        threshold = (int) (DEFAULT_CAPACITY * LOAD_FACTOR);
    }

    private int hash(K key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private int index(int hash, int length) {
        return (length - 1) & hash;
    }

    // ================== PUT ==================
    public void put(K key, V value) {
        int hash = hash(key);
        int index = index(hash, table.length);

        Node<K, V> head = table[index];

        if (head == null) {
            table[index] = new Node<>(hash, key, value, null);
            size++;
        } else {
            Node<K, V> current = head;
            while (true) {
                // TODO 1: key 相同 → 覆盖 value（并 return）
                if (current.hash == hash && (current.key == key || current.key.equals(key))) {
                    current.value = value;
                    return;
                }


                // TODO 2: 到尾部 → 插入新节点（size++）
                if (current.next == null) {
                    current.next = new Node<>(hash
                            , key, value, null);
                    size++;
                    break;
                }

                current = current.next;
            }
        }

        // TODO 3: 判断是否需要扩容
        if (size > threshold) resize();
    }

    // ================== GET ==================
    public V get(K key) {
        int hash = hash(key);
        int index = index(hash, table.length);

        Node<K, V> current = table[index];

        while (current != null) {
            // TODO 4: 找到 key 返回 value
            if (current.hash == hash && (current.key == key || current.key.equals(key))) {
                return current.value;

            }
            current = current.next;

        }

        return null;
    }

    // ================== RESIZE ==================
    private void resize() {
        Node<K, V>[] oldTable = table;
        int oldCap = oldTable.length;
        int newCap = oldCap * 2;
        Node<K, V>[] newTable = new Node[newCap];
        // TODO :遍历旧数组
        for (int i = 0; i < oldCap; i++) {
            Node<K, V> node = oldTable[i];
            // TODO 6 遍历链表，把节点重新分配到newTable;

            while (node != null) {
                int newIndex = index(node.hash, newCap);

                Node<K, V> next = node.next;
                node.next = newTable[newIndex];
                newTable[newIndex] = node;
                node = next;
            }
        }
        table = newTable;
        threshold = (int) (newCap * LOAD_FACTOR);


    }

    public static void main(String[] args) {
        MyHashMap<Integer, String> map = new MyHashMap<>();

        for (int i = 0; i < 20; i++) {
            map.put(i, "v" + i);
        }

        for (int i = 0; i < 20; i++) {
            System.out.println(i + " -> " + map.get(i));
        }
    }
}