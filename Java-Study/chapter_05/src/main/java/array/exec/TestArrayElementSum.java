package array.exec;

/**
 * ClassName: TestArrayElementSum
 * Package: array.exec
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/31 18:54
 * @Version 1.0
 */
public class TestArrayElementSum {
    public static void main(String[] args) {
        int[] arr = {4, 5, 67, 7, 8};
        // 求总和
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        double avg = sum/arr.length;
        System.out.println(avg);
        System.out.println(sum);

    }
}
