package com.gwh;

/** 
 * 冒泡排序
 * 找最大值和最小值 
 * @author gwh 
 * 
 */
public class Sort {
	public static void main(String[] args) {
		Sort s = new Sort();
		double[] d = { 1, 1.5, 2, 3.5, 5, 7, 8, 2.1 }; // 这数组里面的8个数你可以自己修改，
														// 改成你的那8个数
		System.out.println("最大值是：" + s.getMax(d));
		System.out.println("最小值是：" + s.getMin(d));
	}

	public double getMax(double[] d) {
		for (int i = 0; i < d.length - 1; i++) {
			if (d[i] > d[i + 1]) {
				double temp = 0;
				temp = d[i];
				d[i] = d[i + 1];
				d[i + 1] = temp;
			}
		}
		return d[d.length - 1];
	}

	public double getMin(double[] d) {
		for (int i = 0; i < d.length - 1; i++) {
			if (d[i] < d[i + 1]) {
				double temp = 0;
				temp = d[i];
				d[i] = d[i + 1];
				d[i + 1] = temp;
			}
		}
		return d[d.length - 1];
	}
}