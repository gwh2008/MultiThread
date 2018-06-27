package com.gaowh.Counter;

/**
 * 如果使用ThreadLocal管理变量，则每一个使用该变量的线程都获得该变量的副本，
 * 副本之间相互独立，这样每一个线程都可以随意修改自己的变量副本，而不会对其他线程产生影响。
 * @author gwh
 * 
 */
public class Bank {
	// 使用ThreadLocal类管理共享变量account
	private static ThreadLocal<Integer> account = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 100;
		}
	};

	public void save(int money) {
		account.set(account.get() + money);
	}

	public int getAccount() {
		return account.get();
	}
}