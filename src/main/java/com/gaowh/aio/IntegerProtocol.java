package com.gaowh.aio;

import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/*
 *协议编解码
 *通常情况下服务端与客户端通信遵循同一套协议规则，因此我们只需编写一份协议编解码实现即可（如果是跨语言则需要各自实现）。如下所示，协议编解码的需要实现接口Protocol。
 * 代码很简单，一个整数的长度为4byte，所以只要长度大于等于4，我们就能解析到一个整数。
 */
public class IntegerProtocol implements Protocol<Integer> {

    private static final int INT_LENGTH = 4;

    @Override
    public Integer decode(ByteBuffer data, AioSession<Integer> session, boolean eof) {
        if (data.remaining() < INT_LENGTH)
            return null;
        return data.getInt();
    }

    @Override
    public ByteBuffer encode(Integer s, AioSession<Integer> session) {
        ByteBuffer b = ByteBuffer.allocate(INT_LENGTH);
        b.putInt(s);
        b.flip();
        return b;
    }
}