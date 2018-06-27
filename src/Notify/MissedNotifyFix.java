package Notify;


public class MissedNotifyFix extends Object {  
    private Object proceedLock;  
    //该标志位用来指示线程是否需要等待  
    private boolean okToProceed;  
  
    public MissedNotifyFix() {  
        print("in MissedNotify()");  
        proceedLock = new Object();  
        //先设置为false  
        okToProceed = false;  
    }  
  
    public void waitToProceed() throws InterruptedException {  
        print("in waitToProceed() - entered");  
  
        synchronized ( proceedLock ) {  
            print("in waitToProceed() - entered sync block");  
            //while循环判断，这里不用if的原因是为了防止早期通知  
            while ( okToProceed == false ) {  
                print("in waitToProceed() - about to wait()");  
                proceedLock.wait();  
                print("in waitToProceed() - back from wait()");  
            }  
  
            print("in waitToProceed() - leaving sync block");  
        }  
  
        print("in waitToProceed() - leaving");  
    }  
  
    public void proceed() {  
        print("in proceed() - entered");  
  
        synchronized ( proceedLock ) {  
            print("in proceed() - entered sync block");  
            //通知之前，将其设置为true，这样即使出现通知遗漏的情况，也不会使线程在wait出阻塞  
            okToProceed = true;  
            print("in proceed() - changed okToProceed to true");  
            proceedLock.notifyAll();  
            print("in proceed() - just did notifyAll()");  
  
            print("in proceed() - leaving sync block");  
        }  
  
        print("in proceed() - leaving");  
    }  
  
    private static void print(String msg) {  
        String name = Thread.currentThread().getName();  
        System.out.println(name + ": " + msg);  
    }  
  
    public static void main(String[] args) {  
        final MissedNotifyFix mnf = new MissedNotifyFix();  
  
        Runnable runA = new Runnable() {  
                public void run() {  
                    try {  
                        //休眠1000ms，大于runB中的500ms，  
                        //是为了后调用waitToProceed，从而先notifyAll，后wait，  
                        Thread.sleep(1000);  
                        mnf.waitToProceed();  
                    } catch ( InterruptedException x ) {  
                        x.printStackTrace();  
                    }  
                }  
            };  
  
        Thread threadA = new Thread(runA, "threadA");  
        threadA.start();  
  
        Runnable runB = new Runnable() {  
                public void run() {  
                    try {  
                        //休眠500ms，小于runA中的1000ms，  
                        //是为了先调用proceed，从而先notifyAll，后wait，  
                        Thread.sleep(500);  
                        mnf.proceed();  
                    } catch ( InterruptedException x ) {  
                        x.printStackTrace();  
                    }  
                }  
            };  
  
        Thread threadB = new Thread(runB, "threadB");  
        threadB.start();  
  
        try {   
            Thread.sleep(10000);  
        } catch ( InterruptedException x ) {}  
  
        print("about to invoke interrupt() on threadA");  
        threadA.interrupt();  
    }  
}  