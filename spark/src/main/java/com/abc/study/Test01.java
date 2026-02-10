package com.abc.study;

import org.junit.Test;

/**
 * ClassName: Test01
 * Package: com.abc.study
 * Description:
 *
 * @Author JWT
 * @Create 2025/12/18 9:29
 * @Version 1.0
 */
public class Test01 {
    @Test
    public void test01() {
        Parent parent = new Parent();
    }    @Test
    public void test02() {
        new Child();
       /* Parent static var
        Parent static block
        Child static var
        Child static block
        Parent instance var
        Parent instance block
        Parent constructor
        Child instance var
        Child instance block
        Child constructor*/
    /*    四、几个容易混淆的关键点
        1️⃣ 类变量 vs 实例变量
        类型	是否跟随类	执行次数
        static 变量	是	1 次
        实例变量	否	每次 new*/
/*        2️⃣ 代码块的本质
        类型	本质
        静态代码块	类加载时执行
        实例代码块	构造器前执行
        实例变量初始化	本质上也是实例代码块

👉 实例变量初始化 和 实例代码块的执行顺序，取决于它们在代码中的书写顺序*/


/*        五、终极口诀（面试可背）

        先静态，后实例
        先父类，后子类
          变量和代码块按书写顺序
        构造器最后执行
        法靠调用
        */
    }
}
