package src;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectionHandler implements Runnable{
		
	private DataOutputStream clientOutputStream;
	private BufferedReader clientInputReader;
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
			
			//start upd
			udpSocket = new DatagramSocket();
			if (isProvider){
				
				
				//tell client where to send audio
				int udpSockNo = udpSocket.getLocalPort();
				System.out.println("udpSocket: " + udpSockNo);
				clientOutputStream.writeBytes(String.valueOf(udpSockNo)  + '\n');
				clientOutputStream.writeBytes("localhost"  + '\n');
				//clientOutputStream.writeBytes(udpSocket.getInetAddress().toString()); //to send outside of localhost
				
				while(true){
					//receive audio, from place in temp buffer
					buffer = new byte[AudioStreamServerImpl.BUFFER_SIZE];
					DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(incomingPacket);
					server.fillBuffer(incomingPacket.getData());
					//incomingPacket.getData();
					//InetAddress IPAddress = incomingPacket.getAddress();
					//int port = incomingPacket.getPort();
				}
			} else {
				//get connection info from client
				int udpPort = Integer.parseInt(clientInputReader.readLine());
				System.out.println("Received from client udp port no: " + udpPort);
				String ip = clientInputReader.readLine();
				InetAddress IPAddress = InetAddress.getByName(ip);
				
				//send temp buffer to clients
				int index = 0;
				while (true){
					byte[] bytes = server.getAudioByte(index++);
					DatagramPacket sendPacket =
								new DatagramPacket(bytes, bytes.length, IPAddress, udpPort);
						udpSocket.send(sendPacket);
					if (index >= AudioStreamServerImpl.BUFFER_LENGTH) index = 0;
				}
				
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