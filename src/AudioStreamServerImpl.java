package src;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioStreamServerImpl implements AudioStreamServer{

	public static final int DEFAULT_TCP_PORT = 65003;
	public static final int MAX_CONNECTIONS = 1;
	public static final int BUFFER_SIZE = 1024;
	private static final int BUFFER_LENGTH = 10;
	
	private TcpHandler[] tcpConns = new TcpHandler[MAX_CONNECTIONS];
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
		server = new ServerSocket(DEFAULT_TCP_PORT);
		tcpPort = server.getLocalPort();
		tcpIsReady = true;
		while(true){
			Socket client = server.accept();
			tcpConns[numConnected] = new TcpHandler(nextId++, client, this);
			clientThreads[numConnected] = new Thread(tcpConns[numConnected]);
			clientThreads[numConnected++].start();				
		}	
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
	
	
	private class TcpHandler implements Runnable{
		
		private DataOutputStream clientOutputStream;
		private BufferedReader clientInputReader; //TODO: not used?
		private Socket connectionSocket;
		private int nextId;
		private DatagramSocket udpSocket;
		private AudioStreamServer server;
		
		public TcpHandler(int nextId, Socket client, AudioStreamServer server) {
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
			boolean isProvider = (nextId == providerIndex);
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
						buffer = new byte[BUFFER_SIZE];
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
	
}	 