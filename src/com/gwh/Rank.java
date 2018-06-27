package com.gwh;

/**求排列，求各种排列或组合后排列
 * 利用动态规划的思想求排列和组合
 */
import java.util.Arrays;
import java.util.Scanner;

import com.gwh.Copy_2_of_StatisAnyThree;

public class Rank {
	private static boolean f[];

	public static void main(String[] args) {
		System.out.println("请输入字符:");
		Scanner sc = new Scanner(System.in);
		String str = sc.next();
		char[] chars = str.toCharArray();
		// 调用排列组合方法
		Rank s = new Rank();
		s.doit(chars);
	}

	//利用动态规划的思想求排列和组合
	public String[] doit(char[] chars) {
		Scanner sc = new Scanner(System.in);
		System.out.println("请输入个数:");
		String str = "";
		int n = sc.nextInt();
		for (int i = 0; i < chars.length; i++) {
			f = new boolean[chars.length];
			Arrays.fill(f, true);
			for (int j = 0; j < chars.length - 1; j++) {
				chars[j] = chars[j + 1];
			}
			count(chars, str, n);
		}
		String[] array = str.split(",");
		return array;
	}

	/**
	 * 
	 * @param chars
	 *            表示要排列的数组
	 * @param str
	 *            以排列好的字符串
	 * @param n
	 *            个数
	 * 
	 */
	private static void count(char[] chars, String str, int n) {
		if (n == 0) {
			System.out.println(str);
			return;
		}
		for (int i = 0; i < chars.length - 1; i++) {
			if (!f[i]) {
				continue;
			}
			f[i] = false;
			count(chars, str + chars[i], n - 1);
			f[i] = true;
		}
	}
}