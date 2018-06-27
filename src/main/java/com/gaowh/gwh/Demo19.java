package com.gaowh.gwh;

/**求排列，求各种排列或组合后排列
 * 利用【动态规划】的思想求排列和组合
 */
import java.util.Arrays;
import java.util.Scanner;

public class Demo19 {
	private static boolean f[];

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int sz = sc.nextInt();
		for (int i = 0; i < sz; i++) {
			int sum = sc.nextInt();
			f = new boolean[sum];
			Arrays.fill(f, true);
			int[] num = new int[sum];
			for (int j = 0; j < sum; j++) {
				num[j] = j + 1;
			}
			int n = sc.nextInt();
			String str = "";
			count(num, str, n);
		}
	}

	/**
	 * 
	 * @param num
	 *            表示要排列的数组
	 * @param str
	 *            以排列好的字符串
	 * @param n   
	 *            个数
	 *            
	 */
	private static void count(int[] num, String str, int n) {
		if (n == 0) {
			System.out.println(str);
			return;
		}
		for (int i = 0; i < num.length; i++) {
			if (!f[i]) {
				continue;
			}
			f[i] = false;
			count(num, str + num[i], n - 1);
			f[i] = true;
		}
	}
}