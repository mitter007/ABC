package com.abc.static1;

import com.abc.study.Acc;

class Demo {
    private static Acc c = new Acc();
    static int a = 10;  // 静态变量 / 类变量
    int b = 20;         // 实例变量

    public static  Acc getC(){
        return c;
    }

}
