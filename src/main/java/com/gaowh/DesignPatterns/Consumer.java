package com.gaowh.DesignPatterns;

/**
 * 消费者类
 * */
class Consumer implements Runnable {
    private Info info = null;
 
    public Consumer(Info info) {
        this.info = info;
    }
 
    public void run() {
        for (int i = 0; i < 25; ++i) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.info.get();
        }
    }
}