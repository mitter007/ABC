package com;

import org.junit.Test;

/**
 * ClassName: abc
 * Package: com
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/31 18:56
 * @Version 1.0
 */
public class abc {

    @Test
    public void test01() {
        int[] arr = {4, 5, 6, 7};
        long result = 1;
        for (int i = 0; i < arr.length; i++) {
            result *= arr[i];
        }
        System.out.println(result);

    }

    @Test
    public void test02() {
        int[] arr = {4, 5, 6, 7};
        long count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] % 2 == 0) {
                count++;
            }

        }
        System.out.println(count);

    }

    // 求数组元素的最大值
    @Test
    public void test03() {
        int[] arr = {4, 5, 6, 7, 9, 10, 11, 12,6,9,12};
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i]>count){
                count = arr[i];
            }
        }
        System.out.println(count);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == count){
                System.out.println(i);
            }
        }


    }
    @Test
    public void test07() {
        int[] arr = new int[]{1, -2, 3, 10, -4, 7, 2, -5};
        int i = getGreatestSum(arr);
        System.out.println(i);
    }
    public static int getGreatestSum(int[] arr){
        int greatestSum = 0;
        if(arr == null || arr.length == 0){
            return 0;
        }
        int temp = greatestSum;
        for(int i = 0;i < arr.length;i++){
            temp += arr[i];

            if(temp < 0){
                temp = 0;
            }

            if(temp > greatestSum){
                greatestSum = temp;
            }
        }
        if(greatestSum == 0){
            greatestSum = arr[0];
            for(int i = 1;i < arr.length;i++){
                if(greatestSum < arr[i]){
                    greatestSum = arr[i];
                }
            }
        }
        return greatestSum;
    }
    // 杨辉三角
    @Test
    public void test08() {

    }

}
