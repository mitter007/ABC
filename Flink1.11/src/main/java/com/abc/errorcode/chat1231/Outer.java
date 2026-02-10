package com.abc.errorcode.chat1231;

class Outer {
    static Inner inner ;   // 仅声明

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("Inner");
    }

    static class Inner {
        static {
            System.out.println("Inner init");
        }
    }
}
