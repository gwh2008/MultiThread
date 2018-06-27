package BlockingQueue;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 【阻塞栈】与阻塞队列相似，只是它是Java
 * 6中加入的新特性，阻塞栈的接口java.util.concurrent.BlockingDeque也有很多实现类，使用方法也比较相似，具体查看JDK文档。
 * 从结果中可以看出，当添加了第20个元素后，我们从将栈顶元素移处，这样便可以继续向栈中添加元素，之后每添加一个元素，便将栈顶元素移出，这样程序便可以执行结束
 * 。
 * @author gwh
 * 
 */
public class BlockingDequeTest2 {
	public static void main(String[] args) throws InterruptedException {
		BlockingDeque<String> bDeque = new LinkedBlockingDeque<String>(20);
		for (int i = 0; i < 30; i++) {
			// 将指定元素添加到此阻塞栈中
			bDeque.putFirst("" + i);
			System.out.println("向阻塞栈中添加了元素:" + i);
			if (i > 18) {
				// 从阻塞栈中取出栈顶元素，并将其移出
				System.out.println("从阻塞栈中移出了元素：" + bDeque.pollFirst());
			}
		}
		System.out.println("程序到此运行结束，即将退出----");
	}
}