package array.exec;

/**
 * ClassName: Exec_01
 * Package: com.abc.array.exec
 * Description:
 *
 * @Author JWT
 * @Create 2026/3/31 18:45
 * @Version 1.0
 */

//案例1：升景坊单间短期出租4个月，550元/月（水电煤公摊，网费35元/月），
// 空调、卫生间、厨房齐全。屋内均是IT行业人士，喜欢安静。所以要求来租者最好是同行或者刚毕业的年轻人，爱干净、安静。
public class Exec_01 {
    public static void main(String[] args) {
        int[] arr = new int[]{8,2,1,0,3};
        int[] index = new int[]{2,0,3,2,4,0,1,3,2,3,3};
        String tel = "";
        for (int j : index) {
            tel += arr[j];
        }
        System.out.println(tel);

    }

}
