package com.abc.array;

/**
 * ClassName: Simple_14
 * Package: com.abc.array
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/26 19:43
 * @Version 1.0
 */

import java.util.Arrays;

/**
 * 14. 最长公共前缀
 * 简单
 * 相关标签
 * premium lock icon
 * 相关企业
 * 编写一个函数来查找字符串数组中的最长公共前缀。
 * <p>
 * 如果不存在公共前缀，返回空字符串 ""。
 * <p>
 * <p>
 * <p>
 * 示例 1：
 * <p>
 * 输入：strs = ["flower","flow","flight"]
 * 输出："fl"
 * 示例 2：
 * <p>
 * 输入：strs = ["dog","racecar","car"]
 * 输出：""
 * 解释：输入不存在公共前缀。
 * <p>
 * <p>
 * 提示：
 * <p>
 * 1 <= strs.length <= 200
 * 0 <= strs[i].length <= 200
 * strs[i] 如果非空，则仅由小写英文字母组成
 */
public class Simple_14 {
    public String longestCommonPrefix(String[] strs) {
      Arrays.sort(strs);
        int n = strs.length;
        int count = 0;
        for (int i = 0; i < strs[0].toCharArray().length; i++) {
            if (strs[0].charAt(i)==strs[n-1].charAt(i)){
                count++;
            }else {
                break;
            }
        }
        return strs[0].substring(0,count);


    }
}
