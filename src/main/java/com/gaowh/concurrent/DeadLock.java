package com.gaowh.concurrent;
/**
 * 死锁示例程序:
 * 两个同步（synchronized）的方法Name和Hello，
 * 在两个线程中同时调用他们
 * run方法的代码块也必须是synchronized的，
 * 发生死锁
 */
//Name类，其中的sayName方法输出当前线程的名字和字符串Sean
class Name {
    public synchronized static void sayName() {
        System.out.println(Thread.currentThread().getName() + ": Sean! ");
    }
}
//Hello类，其中的sayHello方法输出当前线程的名字和字符串Hello World!
class Hello {
    public synchronized static void sayHello() {
        System.out.println(Thread.currentThread().getName() + ": Hello World! ");
    }
}
//内部类 myRunnable 实现Runnable接口
class myRunnable implements Runnable {
    @Override
    public void run() {
        //持有Name的锁
        synchronized (Name.class) {
            System.out.println("我有了Name锁!");
            for (int i = 0; i < 5; i++) {
            	
                Name.sayName(); 
                Hello.sayHello();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
//继承Runnable接口的类
public class DeadLock implements Runnable {
	    @Override
	    public void run() {
	        //持有Hello的锁
	        synchronized (Hello.class) {
	            System.out.println("我有了Hello锁!");
	            for (int i = 0; i < 5; i++) {
	            	
	            	Hello.sayHello();
	            	Name.sayName();
	            }
	            try {
	                Thread.sleep(10);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    public static void main(String[] args) {
	        myRunnable myrun = new myRunnable();
	        DeadLock test = new DeadLock();
	       
	        Thread ta = new Thread(myrun, "Name");
	        Thread tb = new Thread(test, "Hello");
	        tb.start();
	        ta.start();//main方法中同时启动两个线程
	        
	    }
   
}