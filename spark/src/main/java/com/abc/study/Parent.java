package com.abc.study;

class Parent {
    static int ps = print("Parent static var");

    static {
        print("Parent static block");
    }

    int pi = print("Parent instance var");

    {
        print("Parent instance block");
    }

    Parent() {
        print("Parent constructor");
    }

    static int print(String s) {
        System.out.println(s);
        return 0;
    }
}

class Child extends Parent {
    static int cs = print("Child static var");

    static {
        print("Child static block");
    }

    int ci = print("Child instance var");

    {
        print("Child instance block");
    }

    Child() {
        print("Child constructor");
    }
}
