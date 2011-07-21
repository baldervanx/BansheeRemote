package org.nstamato.bansheeremote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.http.util.ByteArrayBuffer;

/**
 * FIXME: Should base this on http://nitoasync.codeplex.com/ 
 * I.e. port the C# client part to Java here and use the server part in the plugin.
 */
public class SocketManager {

	private String server="";
	private int port=-1;

	private Thread readerThread;
	private SocketChannel socket;
	private OutputStream os;
	private boolean continueserver;
	private BlockingQueue<Message> incomingMessageQueue = new ArrayBlockingQueue<Message>(10);
	private BlockingQueue<Message> outgoingMessageQueue = new ArrayBlockingQueue<Message>(10);
	
	public void connect(String server, int port) throws IOException {
		this.server = server;
		this.port = port;
		//FIXME: Close everything down first if it is open already
		socket = null;
		socket = SocketChannel.open();
		socket.connect(new InetSocketAddress(server,port));
		readerThread = new ReaderThread(socket.getInputStream());
	}
	
	public void write(String command) throws IOException {
		os.write(command.getBytes(), 0, command.length());
		os.flush();
	}
	
	public Message read() {
		return incomingMessageQueue.poll();
	}

	public void disconnect() {
		//TODO: Stop threads and close streams.
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				ErrorHandler.handleErrorQuiet(e);
			}
		}
		socket = null;
	}

	private class ReaderThread extends Thread {
		private static final int MAX_MESSAGE_SIZE = 5*1024;
		private ByteBuffer byteBuff = ByteBuffer.allocateDirect(MAX_MESSAGE_SIZE*2);
		private InputStream is;
		
		public ReaderThread(InputStream inputStream) {
			is = inputStream;
		}

		public void run() {
			while (continueserver) {
				try {
					int byteCount = -1;
					while ((byteCount = is.read(byteBuff, 0, byteBuff.length)) != -1) {
					}
				} catch (Exception e) {
					//FIXME: Cleanup and close down.
					ErrorHandler.handleErrorQuiet(e);
				}
			}
		}
	}
	
}
