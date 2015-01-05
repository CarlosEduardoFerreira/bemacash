package com.kaching123.pos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class SocketPrinter implements PosPrinter{

	private static final int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
    private static final int CONNECTION_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(10);
	
	private Socket socket;
	
	private InputStream is;
	private OutputStream out;

	public SocketPrinter(String host, int port) throws IOException {
		super();
		this.socket = new Socket();
		this.socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
        this.socket.setSoTimeout(READ_TIMEOUT);
		is = socket.getInputStream();
		out = socket.getOutputStream();
	}
	
	@Override
	public void write(byte[] bytes) throws IOException {
		out.write(bytes);
	}

	@Override
	public byte[] read(int len) throws IOException {
		byte[] bytes = new byte[len];
		is.read(bytes);
		return bytes;
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

}
