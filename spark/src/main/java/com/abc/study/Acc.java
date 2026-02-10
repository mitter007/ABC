package com.abc.study;
/*二、单类场景（没有继承）

如果只有一个类：
执行顺序：
静态变量
静态代码块
实例变量
实例代码块
构造器
方法 m() —— 不会执行（除非你调用）*/
public class Acc {
    static int s = print("static var");
    int i = print("instance var");

    static {
        print("static block");
    }

    {
        print("instance block");
    }

    public Acc() {
        print("constructor");
    }

    void m() {
        print("method");
    }

    static int print(String s) {
        System.out.println(s);
        return 0;
    }

    public static void main(String[] args) {
        new Acc();
    }
}
