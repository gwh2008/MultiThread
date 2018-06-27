package Counter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 注：关于Lock对象和synchronized关键字的选择： a.最好两个都不用，使用一种java.util.concurrent包提供的机制，
 * 能够帮助用户处理所有与锁相关的代码。 b.如果synchronized关键字能满足用户的需求，就用synchronized，因为它能简化代码
 * c.如果需要更高级的功能，就用ReentrantLock类，此时要注意及时释放锁，否则会出现死锁，通常在finally代码释放锁
 * 在JavaSE5.0中新增了一个java.util.concurrent包来支持同步。
 * ReentrantLock类是可重入、互斥、实现了Lock接口的锁， 它与使用synchronized方法和快具有相同的基本行为和语义，并且扩展了其能力
 * 
 * @author gwh
 * 
 */
class Bank2 {

	private int account = 100;
	//使用【重入锁】实现线程同步,需要声明这个锁
	private Lock lock = new ReentrantLock();

	public int getAccount() {
		return account;
	}
	// 这里不再需要synchronized
	public void save(int money) {
		lock.lock();
		try {
			account += money;
		} finally {
			lock.unlock();
		}
	}
}