package com.Singleton;

public class Singleton6 {

	private volatile static Singleton6 Singleton;

	private Singleton6() {
	}

	public static Singleton6 getInst() {

		if (Singleton == null) {
			synchronized (Singleton6.class) {
				if (Singleton == null) {
					Singleton = new Singleton6();
				}
			}
		}
		return Singleton;
	}

}
