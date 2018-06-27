package com.gaowh.locks;

import java.util.concurrent.locks.ReentrantLock;  

public class BufferInterruptibly {  
  
    private ReentrantLock lock = new ReentrantLock();  
  
    public void write() {  
        lock.lock();  
        try {  
            long startTime = System.currentTimeMillis();  
            System.out.println("开始往这个buff写入数据…");  
            for (;;)// 模拟要处理很长时间      
            {  
                if (System.currentTimeMillis()  
                        - startTime > Integer.MAX_VALUE) {  
                    break;  
                }  
            }  
            System.out.println("终于写完了");  
        } finally {  
            lock.unlock();  
        }  
    }  
  
    public void read() throws InterruptedException {  
        lock.lockInterruptibly();// 注意这里，可以响应中断      
        try {  
            System.out.println("从这个buff读数据");  
        } finally {  
            lock.unlock();  
        }  
    }  
  
    public static void main(String args[]) {  
        BufferInterruptibly buff = new BufferInterruptibly();  
  
        final Writer2 writer = new Writer2(buff);  
        final Reader2 reader = new Reader2(buff);  
  
        writer.start();  
        reader.start();  
  
        new Thread(new Runnable() {  
  
            @Override  
            public void run() {  
                long start = System.currentTimeMillis();  
                for (;;) {  
                    if (System.currentTimeMillis()  
                            - start > 5000) {  
                        System.out.println("不等了，尝试中断");  
                        reader.interrupt();  //此处中断读操作  
                        break;  
                    }  
                }  
            }  
        }).start();  
  
    }  
}  
  
class Reader2 extends Thread {  
  
    private BufferInterruptibly buff;  
  
    public Reader2(BufferInterruptibly buff) {  
        this.buff = buff;  
    }  
  
    @Override  
    public void run() {  
  
        try {  
            buff.read();//可以收到中断的异常，从而有效退出      
        } catch (InterruptedException e) {  
            System.out.println("我不读了");  
        }  
  
        System.out.println("读结束");  
  
    }  
}  
  
class Writer2 extends Thread {  
  
    private BufferInterruptibly buff;  
  
    public Writer2(BufferInterruptibly buff) {  
        this.buff = buff;  
    }  
  
    @Override  
    public void run() {  
        buff.write();  
    }  
      
}  