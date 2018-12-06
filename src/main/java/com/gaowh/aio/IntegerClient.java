package com.gaowh.aio;

import org.smartboot.socket.transport.AioQuickClient;

/*
 *启动 客户端
 */
public class IntegerClient {
    public static void main(String[] args) throws Exception {
        IntegerClientProcessor processor = new IntegerClientProcessor();
        AioQuickClient<Integer> aioQuickClient = new AioQuickClient<Integer>("localhost", 8888, new IntegerProtocol(), processor);
        aioQuickClient.start();
        processor.getSession().write(1);
        Thread.sleep(1000);
        aioQuickClient.shutdown();
    }
}