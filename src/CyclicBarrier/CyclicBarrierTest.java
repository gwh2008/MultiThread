package CyclicBarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/** 
 * CyclicBarrier [ˈsaɪklɪk][ˈbæriə(r)] 又叫【障碍器】同样是Java
 * 5中加入的新特性，使用时需要导入java.util.concurrent.CylicBarrier。
 * 它适用于这样一种情况：你希望创建一组任务，它们并发地执行工作
 * ，另外的一个任务在这一组任务并发执行结束前一直阻塞等待，直到该组任务全部执行结束，这个任务才得以执行
 * 。这非常像CountDownLatch，只是CountDownLatch是只触发一次的事件，而CyclicBarrier可以多次重用。
 * 
 * 应用场景： 各省数据独立，分库存偖。为了提高计算性能，统计时采用每个省开一个线程先计算单省结果，最后汇总。
 * @author gaowenhui email:gwh_2014@163.com  
 * 
 */
public class CyclicBarrierTest {
	public static void main(String[] args) {
		// 创建CyclicBarrier对象，
		// 并设置执行完一组5个线程的并发任务后，再执行MainTask任务
		CyclicBarrier cb = new CyclicBarrier(5, new MainTask());
		new SubTask("A", cb).start();
		new SubTask("B", cb).start();
		new SubTask("C", cb).start();
		new SubTask("D", cb).start();
		new SubTask("E", cb).start();
	}
}
/**
 * 最后执行的任务
 */
class MainTask implements Runnable {
	public void run() {
		System.out.println("......终于要执行最后的任务了......");
	}
}
/**
 * 一组并发任务
 */
class SubTask extends Thread {
	private String name;
	private CyclicBarrier cb;

	SubTask(String name, CyclicBarrier cb) {
		this.name = name;
		this.cb = cb;
	}
	public void run() {
		System.out.println("[并发任务" + name + "]  开始执行");
		for (int i = 0; i < 999999; i++)
			; // 模拟耗时的任务
		 // 把方法结果存入内存，如ConcurrentHashMap,vector等,代码略   
		System.out.println("[并发任务" + name + "]  开始执行完毕，通知障碍器");
		try {
			// 每执行完一项任务就通知【障碍器】
			cb.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
}