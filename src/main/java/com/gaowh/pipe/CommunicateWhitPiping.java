package com.gaowh.pipe;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * 管道
 * 
 * 主要分为一下步骤：
 * 
 * 首先建立管道流，并将管道流的输入输出对象进行链接；
 * 
 * 将管道流加入到生产对象（线程）中；
 * 
 * 通过管道流引出输入输出流，并在线程中对这些流进行操作；
 * 
 * 注：管道流的的read的方法是一种阻塞方法；
 * 
 * @author gwh
 * 
 */
public class CommunicateWhitPiping {
	public static void main(String[] args) {
		/**
		 * 创建管道输出流
		 */
		PipedOutputStream pos = new PipedOutputStream();
		/**
		 * 创建管道输入流
		 */
		PipedInputStream pis = new PipedInputStream();
		try {
			/**
			 * 将管道输入流与输出流连接 此过程也可通过重载的构造函数来实现
			 */
			pos.connect(pis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/**
		 * 创建生产者线程
		 */
		Producer p = new Producer(pos);
		/**
		 * 创建消费者线程
		 */
		Consumer c = new Consumer(pis);
		/**
		 * 启动线程
		 */
		p.start();
		c.start();
	}
}

/** synchronized,volatile,Reentrantlock,Threadlocal,CyclicBarrier,Semaphore,PipedOutputStream
 * 生产者线程(与一个管道输出流相关联)
 * 
 */
class Producer extends Thread {
	private PipedOutputStream pos;

	public Producer(PipedOutputStream pos) {
		this.pos = pos;
	}

	public void run() {
		int i = 8;
		try {
			pos.write(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/**
 * 消费者线程(与一个管道输入流相关联)
 * 
 */
class Consumer extends Thread {
	private PipedInputStream pis;

	public Consumer(PipedInputStream pis) {
		this.pis = pis;
	}

	public void run() {
		try {
			System.out.println(pis.read());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}