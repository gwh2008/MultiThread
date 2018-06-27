package com.gaowh.concurrent;

import java.util.ArrayList;  
import java.util.List;  
  
public class Resource3 {  
  /**
   * 这段代码运行的时候就会抛出java.util.ConcurrentModificationException错误。
   * 这是因为主线程在遍历list的时候，子线程在向list中添加元素。
   * 那么有没有办法在遍历一个list的时候，还向list中添加元素呢？办法是有的。
   * 就是java concurrent包中的CopyOnWriteArrayList。 Resource4_CopyOnWriteArrayList 示例
   * @param args
   * @throws InterruptedException
   */
    public static void main(String[] args) throws InterruptedException {  
        List<String> a = new ArrayList<String>();  
        a.add("a");  
        a.add("b");  
        a.add("c");  
        final ArrayList<String> list = new ArrayList<String>(  
                a);  
        Thread t = new Thread(new Runnable() {  
            int count = -1;  
  
            @Override  
            public void run() {  
                while (true) {  
                    list.add(count++ + "");  
                }  
            }  
        });  
        t.setDaemon(true);  
        t.start();  
        Thread.currentThread().sleep(3);  
        for (String s : list) {  
            System.out.println(s);  
        }  
    }  
}  