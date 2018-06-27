/**
 * 生产者
 * */
class Producer implements Runnable {
    private Info info = null;
 
    Producer(Info info) {
        this.info = info;
    }
 
    public void run() {
        boolean flag = false;
        for (int i = 0; i < 25; ++i) {
            if (flag) {
                 
                this.info.set("Rollen", 20);
                flag = false;
            } else {
                this.info.set("糖厂", 100);
                flag = true;
            }
        }
    }
}