package com.Singleton;

public class Singleton {

	private volatile static Singleton instence;

	private Singleton() {
	}

	public static Singleton getInstence() {
		if (instence == null) {
			synchronized (Singleton.class) {
				if (instence == null) {
					instence = new Singleton();
				}
			}
		}
		return instence;
	}
}
