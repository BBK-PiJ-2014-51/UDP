package src;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class ConnectionHandler implements Runnable{
		
	private DataOutputStream clientOutputStream;
	private BufferedReader clientInputReader; //TODO: not used?
	private Socket connectionSocket;
	private int nextId;
	private DatagramSocket udpSocket;
	private AudioStreamServer server;
	
	public ConnectionHandler(int nextId, Socket client, AudioStreamServer server) {
		try {
			clientInputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			clientOutputStream = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}	
		this.nextId = nextId;
		connectionSocket = client;
		this.server = server;
	}

	@Override
	public void run() {	
		boolean isProvider = (nextId == server.getProviderIndex());
		try {
			clientOutputStream.writeBytes(String.valueOf(nextId) + '\n');
			clientOutputStream.writeBytes(String.valueOf(
					(isProvider) ? 1 : 0) + '\n');
			
			byte[] buffer;
			if (isProvider){
				//start upd
				udpSocket = new DatagramSocket();
				int udpSockNo = udpSocket.getLocalPort();
				System.out.println("udpSocket: " + udpSockNo);
				clientOutputStream.writeBytes(String.valueOf(udpSockNo)  + '\n');
				clientOutputStream.writeBytes("localhost"  + '\n');
				//clientOutputStream.writeBytes(udpSocket.getInetAddress().toString()); //to send outside of localhost
				
				while(true){
					//receive audio, place in temp buffer
					buffer = new byte[AudioStreamServerImpl.BUFFER_SIZE];
					DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(incomingPacket);
					this.server.fillBuffer(incomingPacket.getData());
					//incomingPacket.getData();
					//InetAddress IPAddress = incomingPacket.getAddress();
					//int port = incomingPacket.getPort();
					
				}
			} else {
				//send temp buffer to clients
				
				/* TODO go on with something like this
				buffer = new byte[BUFFER_SIZE];
				DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
				//receive packet to get Inet and port
				udpSocket.receive(incomingPacket);
				InetAddress IPAddress = incomingPacket.getAddress();
				int port = incomingPacket.getPort();
				*/

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		close();
	}
	
	public boolean close(){
		try {
			if (udpSocket != null) udpSocket.close();
			connectionSocket.close();
			clientOutputStream.close();
			clientInputReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
}