package com.abc;

import org.junit.Test;

import java.util.Arrays;

/**
 * ClassName: ArrayTest
 * Package: com.abc
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/26 19:53
 * @Version 1.0
 */
public class ArrayTest {
    @Test
    public void test() {
        String s = "hello";
        char c = s.charAt(0);
        char c1 = s.charAt(1);
        System.out.println(c);
        System.out.println(c1);
    }

    @Test
    public void test01() {
//       String[] str = {"dog","racecar","car"};
        String[] str = {"flower", "flow", "flight", "fliddd"};
        String s = longestCommonPrefix(str);

        System.out.println(s);
    }

    public String longestCommonPrefix(String[] strs) {
        /**
         *
         * 为什么只需要和最后一个比较呢，因为经过 Arrays.sort(strs); 排序之后，最后一位和第一位是最不一样的
         */
        Arrays.sort(strs);
        int n = strs.length;
        int count = 0;
        for (int i = 0; i < strs[0].length(); ++i) {
            if (strs[0].charAt(i) == strs[n - 1].charAt(i)) { // 相同
                ++count;
            } else { // 不相同
                break;
            }
        }
        return strs[0].substring(0, count);
    }

    @Test
    public void test02() {
        int[] nums = {0, 0, 1, 1, 1, 2, 2, 3, 3, 4};

        System.out.println(removeDuplicates(nums));


    }

    //思路：双指针
    public int removeDuplicates(int[] nums) {
        int slow = 0;
        for (int fast = 1; fast < nums.length; fast++) {
            if (nums[fast] != nums[slow]) {
                slow++;
                nums[slow] = nums[fast];
            }
        }
        return slow + 1;
    }

    public int removeDuplicates2(int[] nums) {
        int i = 1, cursor = 1;
        int pre = nums[0];
        while (cursor < nums.length) {
            if (pre != nums[cursor]) {
                nums[i++] = nums[cursor];
                pre = nums[cursor];
            }
            cursor++;
        }
        return i;
    }

    @Test
    public void test04() {
        int[] nums = {0, 0, 1, 1, 1, 2, 2, 3, 3, 4};
//        int i = removeDuplicates3(nums);
//        System.out.println(i);


    }

    public int removeElement(int[] nums, int val) {
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != val)
                count++;

        }
        return count;
    }

    public int removeElement2(int[] nums, int val) {
        int slow = 0;
        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != val) {
                nums[slow] = nums[fast];
                slow++;
            }
        }
        return slow;
    }

    // 正确
    public int removeElement3(int[] nums, int val) {
        int fastIndex = 0;
        int slowIndex;
        for (slowIndex = 0; fastIndex < nums.length; fastIndex++) {
            if (nums[fastIndex] != val) {
                nums[slowIndex] = nums[fastIndex];
                slowIndex++;
            }
        }
        return slowIndex;
    }

    public int searchInsert(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == target) {
                return i;
            }

        }

        return 1;


    }
}
