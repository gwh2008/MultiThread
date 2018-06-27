package com.gaowh.ProducerConsumer;

public class ThreadCaseDemo{  
    public static void main(String args[]){  
        Info info = new Info(); // 实例化Info对象  
        Producer pro = new Producer(info) ; // 生产者  
        Consumer con = new Consumer(info) ; // 消费者  
        new Thread(pro).start() ;  
        //启动了生产者线程后，再启动消费者线程  
        try{  
            Thread.sleep(500) ;  
        }catch(InterruptedException e){  
            e.printStackTrace() ;  
        }  
  
        new Thread(con).start() ;  
    }  
}  