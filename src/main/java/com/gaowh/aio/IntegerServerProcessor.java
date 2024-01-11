package com.gaowh.aio;

import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;

/*
 *服务端--处理器
 *业务消息的处理需要实现接口MessageProcessor，该接口只有两个方法：process,stateEvent。其中 stateEvent用于定义AioSession状态机的监控与处理。process则会处理每一个接收到的业务消息。
 */
public class IntegerServerProcessor implements MessageProcessor<Integer> {
    @Override
    public void process(AioSession<Integer> session, Integer msg) {
        Integer respMsg = msg + 1;
        System.out.println("接收到客户端数据：" + msg + " ,响应数据:" + (respMsg));
        try {
            session.write(respMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stateEvent(AioSession<Integer> session, StateMachineEnum stateMachineEnum, Throwable throwable) {

    }
}