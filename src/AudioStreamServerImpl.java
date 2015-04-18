package src;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioStreamServerImpl implements AudioStreamServer{

	public static final int CONN_REQUEST = 100;
	public static final int ROLE_REQUEST = 200;
	public static final int DENY_REQUEST = -1;
	public static final int BASE_TCP_PORT = 65000;
	public static final int MAX_CONNECTIONS = 8;
	
	
	private int numConnected = 0;
	private TcpHandler[] tcpConns = new TcpHandler[MAX_CONNECTIONS];
	private Thread[] clientThreads = new Thread[MAX_CONNECTIONS];
	private int tcpPort = -1;
	private boolean tcpIsReady = false;
	private int nextId = 0;
	private int providerIndex = 0;

	public static void main(String[] args) {
		new AudioStreamServerImpl().launch();
	}
	
	@Override
	public int getTcpPort() {
		return tcpPort;
	}
	
	public boolean isTcpReady(){
		return tcpIsReady;
	}
	@Override
	public void run() {
		this.launch();
	}
	
	private void launch() {
			startTcpService();
			
	}
	
	private void startTcpService(){
		ServerSocket server = null;
		while(numConnected < MAX_CONNECTIONS){
			try{
				server = new ServerSocket(0);
				tcpPort = server.getLocalPort();
				tcpIsReady = true;
				Socket client = server.accept();
				tcpConns[numConnected] = new TcpHandler(nextId++, client);
				clientThreads[numConnected] = new Thread(tcpConns[numConnected]);
				clientThreads[numConnected].start();				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			server.close();
			tcpIsReady = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class TcpHandler implements Runnable{
		private DataOutputStream clientOutputStream;
		private BufferedReader clientInputReader; //TODO: not used?
		private Socket connectionSocket;
		private int nextId;
		
		public TcpHandler(int nextId, Socket client) {
			try {
				clientInputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				clientOutputStream = new DataOutputStream(client.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}	
			this.nextId = nextId;
			connectionSocket = client;
		}

		@Override
		public void run() {			
			try {
				numConnected++;
				clientOutputStream.writeBytes(String.valueOf(nextId) + '\n');
				clientOutputStream.writeBytes(String.valueOf(
						(nextId == 0) ? 1 : 0) + '\n');
				//start upd
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public boolean close(){
			try {
				connectionSocket.close();
				clientOutputStream.close();
				clientInputReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}	
}	 