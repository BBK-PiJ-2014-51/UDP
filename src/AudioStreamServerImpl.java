package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class AudioStreamServerImpl implements AudioStreamServer{

	public static final int DEFAULT_TCP_PORT = 65003;
	public static final int MAX_CONNECTIONS = 2;
	public static final int BUFFER_SIZE = 50000; //number of bytes in each byte array
	public static final int BUFFER_LENGTH = 500; //number of byte arrays in buffer
	
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
		buffer[nextBufferWrite++] = data;
		//System.out.println("Writing " + data.toString());
		if (nextBufferWrite == BUFFER_LENGTH)
			nextBufferWrite = 0;
	}
	
	@Override
	public byte[] getAudioByte(int index){
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
	
	private void startTcpService() throws IOException, SocketException{
		if (server == null){
			server = new ServerSocket(DEFAULT_TCP_PORT);
			tcpPort = server.getLocalPort();
		}	
		
		tcpIsReady = true;
		while(numConnected < MAX_CONNECTIONS){
			Socket client = server.accept();
			tcpConns[numConnected] = new ConnectionHandler(nextId++, client, this);
			clientThreads[numConnected] = new Thread(tcpConns[numConnected]);
			clientThreads[numConnected++].start();				
		}
		
		tcpIsReady = false;
		while (numConnected >= MAX_CONNECTIONS){
			try {
				Thread.sleep(100);
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

}	 