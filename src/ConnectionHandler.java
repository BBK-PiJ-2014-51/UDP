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
	private int id;
	private DatagramSocket udpSocket;
	private AudioStreamServer server;
	
	/**
	 * constructs client handler to deal with client connections
	 * 
	 * @param nextId id to assign client
	 * @param client socket client is connected tp
	 * @param server server impl that launched handler
	 */
	public ConnectionHandler(int nextId, Socket client, AudioStreamServer server) {
		try {
			clientInputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			clientOutputStream = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}	
		id = nextId;
		connectionSocket = client;
		this.server = server;
	}
	
	
	/**
	 * manages both audio provider and listener roles on server side
	 */
	@Override
	public void run() {	
		boolean isProvider = (id == server.getProviderIndex());
		try {
			clientOutputStream.writeBytes(String.valueOf(id) + '\n');
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
					
					String received = "received";
					byte[] bytes = received.getBytes();
					DatagramPacket ackPacket =
							new DatagramPacket(bytes, bytes.length, incomingPacket.getAddress(), incomingPacket.getPort());
					udpSocket.send(ackPacket);
					
					server.fillBuffer(incomingPacket.getData()); 
				}
			} else {
				//get connection info from client
				int udpPort = Integer.parseInt(clientInputReader.readLine());
				System.out.println("Received from client udp port no: " + udpPort);
				String ip = clientInputReader.readLine();
				InetAddress IPAddress = InetAddress.getByName(ip);
				
				//send temp buffer to clients
				//int index = 0;
				while (true){
					System.out.println("Packet requested by " + id);
					byte[] bytes = server.getNextAudioByte();
					DatagramPacket sendPacket =
								new DatagramPacket(bytes, bytes.length, IPAddress, udpPort);
					udpSocket.send(sendPacket);
					
					//notify of receipt or loop back
					byte[] response = new byte[8];
					boolean sent = false;
					DatagramPacket ackPacket = new DatagramPacket(response, response.length);
					udpSocket.receive(ackPacket);
					String ack = new String(ackPacket.getData());
					if(ack.equals("received")) server.clientReceived(id);
					System.out.println("Packet received by client " + id);
	
					while (!server.udpIsReady()) {
						System.out.println("ready = " + server.udpIsReady());
						Thread.sleep(2);
					}		
				}
				

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		close();
	}
	
	/**
	 * closes resources used by handler
	 * 
	 * @return true on completion
	 */
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