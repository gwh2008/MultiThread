package BlockingQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 阻塞队列是Java 5并发新特性中的内容，阻塞队列的接口是java.util.concurrent.BlockingQueue，它有多个实现类：
 * ArrayBlockingQueue
 * 、DelayQueue、LinkedBlockingQueue、PriorityBlockingQueue、SynchronousQueue等
 * ，用法大同小异
 * ，具体可查看JDK文档，这里简单举例看下ArrayBlockingQueue，它实现了一个有界队列，当队列满时，便会阻塞等待，直到有元素出队，
 * 后续的元素才可以被加入队列。
 * 
 * @author gwh
 * 
 */
public class BlockingQueueTest {
	public static void main(String[] args) throws InterruptedException {
		BlockingQueue<String> bqueue = new ArrayBlockingQueue<String>(20);
		for (int i = 0; i < 30; i++) {
			// 将指定元素添加到此队列中
			bqueue.put("加入元素" + i);
			System.out.println("向阻塞队列中添加了元素:" + i);
		}
		System.out.println("程序到此运行结束，即将退出----");
	}
}