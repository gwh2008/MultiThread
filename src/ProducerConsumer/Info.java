package ProducerConsumer;

import java.util.concurrent.*;  
import java.util.concurrent.locks.*;  
  
class Info{ // 定义信息类  
    private String name = "name";//定义name属性，为了与下面set的name属性区别开  
    private String content = "content" ;// 定义content属性，为了与下面set的content属性区别开  
    private boolean flag = true ;   // 设置标志位,初始时先生产  
    private Lock lock = new ReentrantLock();    
    private Condition condition = lock.newCondition(); //产生一个Condition对象  
    public  void set(String name,String content){  
        lock.lock();  
        try{  
            while(!flag){  
                condition.await() ;  
            }  
            this.setName(name) ;    // 设置名称  
            Thread.sleep(300) ;  
            this.setContent(content) ;  // 设置内容  
            flag  = false ; // 改变标志位，表示可以取走  
            condition.signal();  
        }catch(InterruptedException e){  
            e.printStackTrace() ;  
        }finally{  
            lock.unlock();  
        }  
    }  
  
    public void get(){  
        lock.lock();  
        try{  
            while(flag){  
                condition.await() ;  
            }     
            Thread.sleep(300) ;  
            System.out.println(this.getName() +   
                " --> " + this.getContent()) ;  
            flag  = true ;  // 改变标志位，表示可以生产  
            condition.signal();  
        }catch(InterruptedException e){  
            e.printStackTrace() ;  
        }finally{  
            lock.unlock();  
        }  
    }  
  
    public void setName(String name){  
        this.name = name ;  
    }  
    public void setContent(String content){  
        this.content = content ;  
    }  
    public String getName(){  
        return this.name ;  
    }  
    public String getContent(){  
        return this.content ;  
    }  
}  