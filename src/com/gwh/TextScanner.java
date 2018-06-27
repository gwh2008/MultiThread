package com.gwh;

import java.util.Scanner;

public class TextScanner {
	public static void main(String[] args) {
		// 创建Scanner对象 接受从控制台输入
		Scanner input = new Scanner(System.in);
		System.out.println("请输入名字:");
		// 接受String型
		String name = input.next();
		System.out.println("请输入学号");
		// 接受int型
		int id = input.nextInt();// 什么类型next后面就接什么 注意大小写
		// 输出结果
		System.out.println("名字为:" + name + "\t学号为:" + id);
	}
}