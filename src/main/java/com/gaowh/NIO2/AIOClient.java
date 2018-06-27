package com.gaowh.NIO2;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class AIOClient {

	public static void main(String... args) throws Exception {
		AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
		client.connect(new InetSocketAddress("localhost", 9888)).get();
		client.write(ByteBuffer.wrap("123456789".getBytes()));
		Thread.sleep(111111111);
	}
}