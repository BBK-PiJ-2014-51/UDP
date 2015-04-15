package src;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioStreamServerImpl implements AudioStreamServer{

	public static final int CONN_REQUEST = 100;
	public static final int DENY_REQUEST = -1;
	public static final int BASE_TCP_PORT = 65000;
	public static final int MAX_CONNECTIONS = 8;
	
	private int numConnected = 0;
	private Thread[] audioClients = new Thread[MAX_CONNECTIONS];
	private int tcpPort = -1;
	private boolean tcpIsReady = false;

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
			try (ServerSocket port = new ServerSocket(0)){
				tcpPort = port.getLocalPort();
				tcpIsReady = true;
				Socket connectionSocket = port.accept();
				tcpIsReady = false;
				try (BufferedReader clientInputReader = new BufferedReader(
								new InputStreamReader(connectionSocket.getInputStream()));
					DataOutputStream clientOutputStream = new DataOutputStream(
							connectionSocket.getOutputStream());)
				{
					while(true){
						String reqSeq = clientInputReader.readLine();
						int requestCode = Integer.valueOf(reqSeq);
						if (requestCode == CONN_REQUEST){
							clientOutputStream.writeBytes(String.valueOf(numConnected) + '\n');
							// TODO write int?
							// TODO: properly get correct index no
							audioClients[numConnected] = new Thread(
									new TcpHandler());
							audioClients[numConnected++].start();
		    				break;
						}
					}	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private class TcpHandler implements Runnable{
		
		@Override
		public void run() {			
		
		}	
	}	
	
}	 
