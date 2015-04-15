package tests;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import src.AudioStreamClient;
import src.AudioStreamClientImpl;
import src.AudioStreamServer;
import src.AudioStreamServerImpl;

public class AudioStreamServerTest {

	/**
	 * Instantiates server and tests that the default 
	 * port number is returned as the current listening port.
	 */
	@Test
	public void getTcpPort(){
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread sT = new Thread(server);
		sT.start();
		while (!server.isTcpReady());
		int tcpPort = server.getTcpPort();
		boolean isValid = (tcpPort > 0);
		assertEquals(true, isValid);
	}
	
	/**
	 * Mimics a client by requesting an id from the server instance.
	 * Tests that the id received is 0 (first assigned id).      
	 */
	@Test
	public void startTcpService(){
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread sT = new Thread(server);
		sT.start();
		
		Socket clientSocket = null;
		DataOutputStream outputToServer = null;
		BufferedReader inputFromServer = null;
		int returnVal = -1;
		try {
			while (!server.isTcpReady());
			int tcpPort = server.getTcpPort();
			clientSocket = new Socket("localhost", tcpPort);
			outputToServer = 
					new DataOutputStream(clientSocket.getOutputStream());
			inputFromServer = 
					new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//outputToServer.writeBytes(String.valueOf(AudioStreamServerImpl.CONN_REQUEST));
			//outputToServer.writeBytes("\n");
	        returnVal = Integer.parseInt(inputFromServer.readLine());
			inputFromServer.close();
			outputToServer.close();
			clientSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(0, returnVal);
	}
	
	/**
	 * Mimics several clients requesting ids from the server.
	 * Tests that the ids received are 0-3.      
	 */
	@Test
	public void connectSeveralClients() throws IOException{
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread[] clientThreads = new Thread[4];
		AudioStreamClient[] clients = new AudioStreamClient[4];
		int[] expectedIds = new int[4];
		Thread serverTh = new Thread(server);
		serverTh.start();
		
		boolean unexpectedId = false;
		for (int i = 0; i < AudioStreamServerImpl.MAX_CONNECTIONS; i++){
			Socket clientSocket = null;
			DataOutputStream outputToServer = null;
			BufferedReader inputFromServer = null;
			int returnVal = -1;
			try {
				while (!server.isTcpReady());
				int tcpPort = server.getTcpPort();
				clientSocket = new Socket("localhost", tcpPort);
				outputToServer = 
						new DataOutputStream(clientSocket.getOutputStream());
				inputFromServer = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				//outputToServer.writeBytes(String.valueOf(AudioStreamServerImpl.CONN_REQUEST));
				//outputToServer.writeBytes("\n");
		        returnVal = Integer.parseInt(inputFromServer.readLine());
				inputFromServer.close();
				outputToServer.close();
				clientSocket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
			if (returnVal != i) unexpectedId = true;
		}	
		
		assertEquals(false, unexpectedId);
	}
	
	@Test
	public void isProviderTest(){
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread sT = new Thread(server);
		sT.start();
		Socket clientSocket = null;
		DataOutputStream outputToServer = null;
		BufferedReader inputFromServer = null;
		int returnVal = -1;
		int isProvider = -1;
		try {
			while (!server.isTcpReady());
			int tcpPort = server.getTcpPort();
			clientSocket = new Socket("localhost", tcpPort);
			outputToServer = 
					new DataOutputStream(clientSocket.getOutputStream());
			inputFromServer = 
					new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        
			int id = Integer.parseInt(inputFromServer.readLine());
	        outputToServer.writeBytes(String.valueOf(AudioStreamServerImpl.ROLE_REQUEST));
			outputToServer.writeBytes("\n");
			isProvider = Integer.parseInt(inputFromServer.readLine());
	        inputFromServer.close();
			outputToServer.close();
			clientSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(1, isProvider);
	}
	
	@Test
	public void testRoles() throws IOException{
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread[] clientThreads = new Thread[4];
		AudioStreamClient[] clients = new AudioStreamClient[4];
		int[] expectedIds = new int[4];
		Thread serverTh = new Thread(server);
		serverTh.start();
		
		boolean unexpectedRole = false;
		for (int i = 0; i < AudioStreamServerImpl.MAX_CONNECTIONS; i++){
			Socket clientSocket = null;
			DataOutputStream outputToServer = null;
			BufferedReader inputFromServer = null;
			int returnVal = -1;
			int isProvider = -1;
			try {
				while (!server.isTcpReady());
				int tcpPort = server.getTcpPort();
				clientSocket = new Socket("localhost", tcpPort);
				outputToServer = 
						new DataOutputStream(clientSocket.getOutputStream());
				inputFromServer = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				//outputToServer.writeBytes(String.valueOf(AudioStreamServerImpl.CONN_REQUEST));
				//outputToServer.writeBytes("\n");
		        returnVal = Integer.parseInt(inputFromServer.readLine());
		        outputToServer.writeBytes(String.valueOf(AudioStreamServerImpl.ROLE_REQUEST));
				outputToServer.writeBytes("\n");
		        isProvider = Integer.parseInt(inputFromServer.readLine());
				inputFromServer.close();
				outputToServer.close();
				clientSocket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
			if (i == 0 && isProvider != 1 ) unexpectedRole = true;
			else if (i != 0 && isProvider == 1 ) unexpectedRole = true;
		}	
		
		assertEquals(false, unexpectedRole);
	}
}