package sharethread;

/** * 通过内部类实现线程的共享变量 * */
public class Innersharethread {
	public static void main(String[] args) {
		Mythread mythread = new Mythread();
		mythread.getThread().start();
		mythread.getThread().start();
		mythread.getThread().start();
		mythread.getThread().start();
	}
}

class Mythread {
	int index = 0;

	private class InnerThread extends Thread {
		public synchronized void run() {
			while (true) {
				System.out.println(Thread.currentThread().getName()
						+ "is running and index is " + index++);
			}
		}
	}

	public Thread getThread() {
		return new InnerThread();
	}
}
// 在这其中内部类共享类公共类里面的index变量，并通过对公共类进行加锁达到方法同步的目的。