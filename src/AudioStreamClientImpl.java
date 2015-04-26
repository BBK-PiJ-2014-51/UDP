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
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioStreamClientImpl implements AudioStreamClient {
	private int id;
	private boolean isProvider;
	
	private Socket clientSocket;
	private BufferedInputStream in;
	private ByteArrayOutputStream out;
		
	public AudioStreamClientImpl(){
		try {
			clientSocket = new Socket("localhost", AudioStreamServerImpl.DEFAULT_TCP_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		AudioStreamClient client = new AudioStreamClientImpl();
		client.connect();
	}
	
	@Override
	public int getId(){
		return id;
	}
	
	@Override
	public boolean isAudioProvider(){
		return isProvider;
	}
	
	@Override
	public boolean connect(){
		try {
			DataOutputStream outputToServer = 
					new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inputFromServer = 
					new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
			id = Integer.parseInt(inputFromServer.readLine());
			isProvider = (inputFromServer.readLine().equals("1"));
			System.out.println("isProv val: " + isProvider);
			
			if (isProvider){
				
				DatagramSocket udpSocket = new DatagramSocket();
				
				System.out.println("this client is a provider");
				int udpPort = Integer.parseInt(inputFromServer.readLine());
				System.out.println("Received from server udp port no: " + udpPort);
				String ip =inputFromServer.readLine();
				InetAddress IPAddress = InetAddress.getByName(ip);
				
				loadSong();
				//send audio
				while (true){
					byte[] bytes = getNextAudioBytes();
					DatagramPacket sendPacket =
								new DatagramPacket(bytes, bytes.length, IPAddress, udpPort);
						udpSocket.send(sendPacket);
					}
				
			} else {
				//receive and play audio
				
				//open udp connection
				DatagramSocket udpSocket = new DatagramSocket();
				
				//notify server where to send audio
				int udpSockNo = udpSocket.getLocalPort();
				System.out.println("client udpSocket open at: " + udpSockNo);
				outputToServer.writeBytes(String.valueOf(udpSockNo)  + '\n');
				outputToServer.writeBytes("localhost"  + '\n');
				
				//receive and playback from server 
				AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
	            DataLine.Info dInfo = new DataLine.Info(SourceDataLine.class, format, 100000);
	            SourceDataLine soundLine = null;
	            try {
					soundLine = (SourceDataLine) AudioSystem.getLine(dInfo);
					soundLine.open(format, 100000);
					soundLine.start();
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
				byte[] buffer;
				int bufferOffset = 0;
				while(true){
					//receive audio, from place in temp buffer
					buffer = new byte[AudioStreamServerImpl.BUFFER_SIZE];
					DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(incomingPacket);
		            
		            buffer = incomingPacket.getData();
		            //soundLine.drain();
		            soundLine.write(buffer, 0, buffer.length);
		            
					//System.out.println("Client has received: " + incomingPacket.getData().toString());
				}
				
			}
			
			//inputFromServer.close(); // TODO unreachable error
			//outputToServer.close();
			//clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	

	@Override
	public void run() {
		connect();		
	}
	
	private void loadSong() {
		try {
			in = new BufferedInputStream(new FileInputStream("louis.wav"));
			out = new ByteArrayOutputStream();
		} catch (FileNotFoundException e) {
				e.printStackTrace();
		} 
	}

	private byte[] getNextAudioBytes() {
		byte[] buffer = new byte[AudioStreamServerImpl.BUFFER_SIZE];
		try {
			int read = in.read(buffer);
			
			if (read == -1) {
				loadSong();
				getNextAudioBytes();
			}
			out.write(buffer);
			
			byte[] nextBytes = out.toByteArray();
			out.reset();
			return nextBytes;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
