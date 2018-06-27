package com.gwh;
public class Factory1 {
	public static Car getCarInstance(String type) {
		Car c = null;
		try {
			c = (Car) Class.forName("com.gwh." + type)
			.newInstance();// 利用反射得到汽车类型　
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return c;
	}	
}
