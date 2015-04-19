package src;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class AudioStreamClientImpl implements AudioStreamClient {
	private Socket clientSocket;
	private int id;
	private boolean isProvider;
	
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
}
