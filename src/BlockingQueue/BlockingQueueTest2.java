package BlockingQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 如果队列已满后，我们将队首元素移出，并可以继续向阻塞队列中添加元素
 * 从结果中可以看出，当添加了第20个元素后，我们从队首移出一个元素，这样便可以继续向队列中添加元素，之后每添加一个元素，便从将队首元素移除，
 * 这样程序便可以执行结束。
 * 
 * @author gwh
 * 
 */
public class BlockingQueueTest2 {
	public static void main(String[] args) throws InterruptedException {
		BlockingQueue<String> bqueue = new ArrayBlockingQueue<String>(20);
		for (int i = 0; i < 30; i++) {
			// 将指定元素添加到此队列中
			bqueue.put("" + i);
			System.out.println("向阻塞队列中添加了元素:" + i);
			if (i > 18) {
				// 从队列中获取队头元素，并将其移出队列
				System.out.println("从阻塞队列中移除元素：" + bqueue.take());
			}
		}
		System.out.println("程序到此运行结束，即将退出----");
	}
}