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
			//start upd
	}
	
	private void startTcpService(){
		while(numConnected < MAX_CONNECTIONS){
			try{
				ServerSocket port = new ServerSocket(0);
				tcpPort = port.getLocalPort();
				tcpIsReady = true;
				Socket connectionSocket = port.accept();
				tcpIsReady = false;
				BufferedReader clientInputReader = new BufferedReader(
					new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream clientOutputStream = new DataOutputStream(
					connectionSocket.getOutputStream());
				tcpConns[numConnected] = new TcpHandler(nextId++, port, connectionSocket, 
						clientInputReader, clientOutputStream);
				clientThreads[numConnected] = new Thread(tcpConns[numConnected]);
				clientThreads[numConnected].start();				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class TcpHandler implements Runnable{
		private DataOutputStream clientOutputStream;
		private BufferedReader clientInputReader;
		private ServerSocket port;
		private Socket connectionSocket;
		private int nextId;
		
		public TcpHandler(int nextId, ServerSocket port, Socket connectionSocket, 
				BufferedReader clientInputReader, DataOutputStream clientOutputStream) {
			this.nextId = nextId;
			this.port = port;
			this.connectionSocket = connectionSocket;
			this.clientOutputStream = clientOutputStream;
			this.clientInputReader = clientInputReader;
		}

		@Override
		public void run() {			
			try {
				numConnected++;
				clientOutputStream.writeBytes(String.valueOf(nextId) + '\n');
				Thread.sleep(35); // TODO find better way to wait for client input
				int request = Integer.valueOf(clientInputReader.readLine());
				if (request == ROLE_REQUEST)
					clientOutputStream.writeBytes(String.valueOf(
							(nextId == 0) ? 1 : 0) + '\n');
				close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO remove with sleep timer
				e.printStackTrace();
			}
		}
		
		public boolean close(){
			try {
				port.close();
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