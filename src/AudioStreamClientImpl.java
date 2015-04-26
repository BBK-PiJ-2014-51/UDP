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
			System.out.println("isProv: " + isProvider);
			int udpPort = Integer.parseInt(inputFromServer.readLine());
			System.out.println("Received: " + udpPort);
			DatagramSocket udpSocket = new DatagramSocket();
			String ip =inputFromServer.readLine();
			InetAddress IPAddress = InetAddress.getByName(ip);
			
			if (isProvider){
				System.out.println("Is provider");
				loadSong();
				//send audio
				while (true){
					byte[] bytes = getNextAudioBytes();
					DatagramPacket sendPacket =
								new DatagramPacket(bytes, bytes.length, IPAddress, udpPort);
						udpSocket.send(sendPacket);
					}
				}
			else {
				//receive and play audio
			}
			udpSocket.close();
			inputFromServer.close();
			outputToServer.close();
			clientSocket.close();
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
			in = new BufferedInputStream(new FileInputStream("test.mp3"));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
