package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class AudioStreamServerImpl implements AudioStreamServer{

	public static final int DEFAULT_TCP_PORT = 65000;
	public static final int MAX_CONNECTIONS = 8;
	public static final int BUFFER_SIZE = 65000; //number of bytes in each byte array
	public static final int BUFFER_LENGTH = 512; //number of byte arrays in buffer
	
	private ConnectionHandler[] tcpConns = new ConnectionHandler[MAX_CONNECTIONS];
	private int numConnected = 0;
	private Thread[] clientThreads = new Thread[MAX_CONNECTIONS];
	private int nextId = 0;
	
	private boolean tcpIsReady = false;
	private int tcpPort = -1;
	private ServerSocket server = null;

	private byte[][] buffer = new byte[BUFFER_LENGTH][BUFFER_SIZE];
	private int providerIndex = 0;
	private int nextBufferWrite = 0;

	private boolean[] receivedLast = new boolean[MAX_CONNECTIONS - 1];
	private static int nextBufferRead = 0;
	private int acks = 0;
	
	private boolean udpReady = true;
	
	private Socket client = null;
	
	public static void main(String[] args) {
		new AudioStreamServerImpl().run();
	}
	
	@Override
	public int getTcpPort() {
		return tcpPort;
	}
	
	@Override
	public int getProviderIndex() {
		return providerIndex;
	}
	
	@Override
	public void fillBuffer(byte[] data){
		System.out.println("Writing to buffer at index " + nextBufferWrite);
		buffer[nextBufferWrite++] = data;
		while (nextBufferWrite > (nextBufferRead + 10) % BUFFER_LENGTH ){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (nextBufferWrite == BUFFER_LENGTH)
			nextBufferWrite = 0;
	}
	
	@Override
	public byte[] getAudioByte(int index){
		System.out.println("Reading from buffer at index " + index);
		return buffer[index];
	}
	
	public boolean isTcpReady(){
		return tcpIsReady;
	}
	
	@Override
	public void run() {
		try {
			startTcpService();
		} catch (SocketException se){
			System.out.println("Terminating tcp service: " + se.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeTcpService();
	}
	
	@Override
	public int getNumConnected(){
		return numConnected;
	}
	
	@Override
	public void clientReceived(int id) {
		acks++;
		System.out.println("client " + id + " received packet");
		checkReceived();
	}
	
	private void checkReceived(){		
		if (acks == numConnected - 1){
			System.out.println("All received");
			for (int i = 0; i < receivedLast.length; i++){
				receivedLast[i] = false;
			}
			
			nextBufferRead++;
			udpReady = true;
			acks = 0;
		}
	}

	@Override
	public byte[] getNextAudioByte() {
		if (nextBufferRead > nextBufferWrite){
			String response = "reconnec";
			tcpConns[0] = null;
			clientThreads[0] = null;
			nextId = nextBufferRead = nextBufferWrite = numConnected = 0;
			return response.getBytes();
		}
		System.out.println("reading at " + nextBufferRead);
		if (nextBufferRead == BUFFER_LENGTH) nextBufferRead = 0;
		return buffer[nextBufferRead];
		
	}
	
	private void startTcpService() throws IOException, SocketException{
		if (server == null){
			server = new ServerSocket(DEFAULT_TCP_PORT);
			tcpPort = server.getLocalPort();
		}	
		
		tcpIsReady = true;
		while(numConnected < MAX_CONNECTIONS){
			client = server.accept();
			tcpConns[numConnected] = new ConnectionHandler(nextId++, client, this);
			clientThreads[numConnected] = new Thread(tcpConns[numConnected]);
			clientThreads[numConnected++].start();				
		}
		
		tcpIsReady = false;
		while (numConnected >= MAX_CONNECTIONS){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		startTcpService();
	}
	
	public void closeTcpService(){
		tcpIsReady = false;
		
		if(server != null)
			try {

				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	public boolean udpIsReady() {
		return udpReady;
	}
}	 