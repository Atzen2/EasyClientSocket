package easyClientSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class EasyClientSocket {

	private Socket socket;
	private Thread receiveThread;
	private PrintWriter outputStream;
	private BufferedReader inputStream;
	private EasyClientSocketMessageReceiver receiver = null;
	private TransportAddress localTransportAddress;;
	private TransportAddress remoteTransportAddress;
	
	
	public class TransportAddress{
		private String address;
		private int port;
		
		public TransportAddress(String address, int port) {
			this.address = address;
			this.port = port;
		}
		
		public String getAddress() {
			return address;
		}
		
		public int getPort() {
			return port;
		}
	}

	
	
	public EasyClientSocket(String address, int port, EasyClientSocketMessageReceiver receiver) throws UnknownHostException, IOException {
		createSocket(address, port);
		setReceiver(receiver);
		createOutputStream();
		createInputStream();
		createReceiveThread();
		setTransportAddresses();
	}
	
	private void createSocket(String address, int port) throws UnknownHostException, IOException {
		socket = new Socket(address, port);
	}
	
	private void setReceiver(EasyClientSocketMessageReceiver receiver) {
		this.receiver = receiver;
	}
	
	private void createOutputStream() throws IOException {
		outputStream = new PrintWriter(socket.getOutputStream(), true);
	}
	
	private void createInputStream() throws IOException {
		inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	private void createReceiveThread() {
		receiveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					while (!Thread.interrupted()) {
						String input = inputStream.readLine();
						if(receiver != null) receiver.onMessageReceived(input);
					}

				} catch (IOException e) {
					if(!e.getMessage().equals("Socket closed")) // Thrown from BufferReader. Recommended way to interrupt it
						e.printStackTrace();
				}

			}
		});
		receiveThread.start();
	}

	private void setTransportAddresses(){
		remoteTransportAddress = new TransportAddress(socket.getInetAddress().getHostAddress(), socket.getPort());
		localTransportAddress = new TransportAddress(socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
	}
	
	
	
	public void send(String message) {
		outputStream.println(message);
	}

	
	
	public void close() throws IOException {
		socket.close();
		receiveThread.interrupt();
	}
	
	
	
	public TransportAddress getRemoteAddress() {
		return remoteTransportAddress;
	}
	
	
	
	public TransportAddress getLocalAddress() {
		return localTransportAddress;
	}
}
