package com.gaowh.gwh;

public class Benz implements Car {

	@Override
	public void run() {
		   System.out.println("Benz开始启动了。。。。。");
	}

	@Override
	public void stop() {
		 System.out.println("Benz停车了。。。。。");
	}

}
