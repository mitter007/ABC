package com.abc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 针对单个 (syscode, groupcode, firstDim) 的正态性判断工具类
 *
 * 输入：List<Integer>（size: 0 ~ 2880）
 * 输出：是否近似服从正态分布
 *
 * 工程约定：
 * - n < 30 直接视为非正态
 * - n >= 50 使用 D’Agostino K²
 */
public final class NormalityJudgeUtils {

    private NormalityJudgeUtils() {}

    /* ====================== 对外主入口 ====================== */

    public static boolean isNormal(List<Integer> values) {
        if (!isValid(values)) {
            return false;
        }

        int n = values.size();

        if (n < 30) {
            return false;
        }

        if (n >= 50) {
            return isNormalByDAgostino(values);
        }

        return isApproximatelyNormal(values);
    }

    /* ====================== D’Agostino K² ====================== */

    private static boolean isNormalByDAgostino(List<Integer> values) {
        int n = values.size();
        if (n < 20) return false;

        double mean = mean(values);

        double m2 = 0, m3 = 0, m4 = 0;
        for (int x : values) {
            double d = x - mean;
            double d2 = d * d;
            m2 += d2;
            m3 += d2 * d;
            m4 += d2 * d2;
        }

        m2 /= n;
        m3 /= n;
        m4 /= n;

        if (m2 == 0) {
            return true; // 全相等，工程上视为正态
        }

        double skewness = m3 / Math.pow(m2, 1.5);
        double kurtosis = m4 / (m2 * m2) - 3;

        double zSkew = skewness / Math.sqrt(6.0 / n);
        double zKurt = kurtosis / Math.sqrt(24.0 / n);

        double k2 = zSkew * zSkew + zKurt * zKurt;

        // χ²(df=2, α=0.05) = 5.991
        return k2 < 5.991;
    }

    /* ====================== 工程近似判断 ====================== */

    private static boolean isApproximatelyNormal(List<Integer> values) {
        int n = values.size();
        if (n < 30) return false;

        double mean = mean(values);
        double median = median(values);
        double std = std(values, mean);

        if (std == 0) return true;

        double skew = skewness(values, mean, std);
        double kurt = kurtosis(values, mean, std);

        return Math.abs(mean - median) / std < 0.1
            && Math.abs(skew) < 0.5
            && Math.abs(kurt) < 1.0;
    }

    /* ====================== 基础统计量 ====================== */

    private static double mean(List<Integer> values) {
        double sum = 0;
        for (int x : values) sum += x;
        return sum / values.size();
    }

    private static double median(List<Integer> values) {
        List<Integer> copy = new ArrayList<>(values);
        Collections.sort(copy);
        int n = copy.size();
        if (n % 2 == 0) {
            return (copy.get(n / 2 - 1) + copy.get(n / 2)) / 2.0;
        }
        return copy.get(n / 2);
    }

    private static double std(List<Integer> values, double mean) {
        double sum = 0;
        for (int x : values) {
            double d = x - mean;
            sum += d * d;
        }
        return Math.sqrt(sum / values.size());
    }

    private static double skewness(List<Integer> values, double mean, double std) {
        if (std == 0) return 0;
        double sum = 0;
        for (int x : values) {
            sum += Math.pow((x - mean) / std, 3);
        }
        return sum / values.size();
    }

    private static double kurtosis(List<Integer> values, double mean, double std) {
        if (std == 0) return 0;
        double sum = 0;
        for (int x : values) {
            sum += Math.pow((x - mean) / std, 4);
        }
        return sum / values.size() - 3;
    }

    /* ====================== 校验 ====================== */

    private static boolean isValid(List<Integer> values) {
        return values != null && !values.isEmpty();
    }
}
