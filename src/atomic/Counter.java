package atomic;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
/**
 *使用CAS实现原子操作，线程安全计数器 
 * @author Administrator
 *
 */
public class Counter {
	private AtomicInteger ai = new AtomicInteger();
	private int i = 0;

	public static void main(String[] args) {
		final Counter cas = new Counter();
		List<Thread> ts = new ArrayList<Thread>();
		// 添加100个线程
		for (int j = 0; j < 100; j++) {
			ts.add(new Thread(new Runnable() {
				public void run() {
					// 执行100次计算，预期结果应该是10000
					for (int i = 0; i < 100; i++) {
						cas.count();
						cas.safeCount();
					}
				}
			}));
		}
		//开始执行
		for (Thread t : ts) {
			t.start();
		}
		// 等待所有线程执行完成
		for (Thread t : ts) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("非线程安全计数结果："+cas.i);
		System.out.println("线程安全计数结果："+cas.ai.get());
	}

	/** 使用CAS实现线程安全计数器 */
	private void safeCount() {
		for (;;) {
			int i = ai.get();
			// 如果当前值 == 预期值，则以原子方式将该值设置为给定的更新值
			boolean suc = ai.compareAndSet(i, ++i);
			if (suc) {
				break;
			}
		}
	}

	/** 非线程安全计数器 */
	private void count() {
		i++;
	}
}
//结果：
//非线程安全计数结果：9867
//线程安全计数结果：10000
