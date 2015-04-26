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
	 * Instantiates server and tests that a valid 
	 * port number is returned as the current listening port.
	 */
	@Test
	public void getTcpPort(){
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread sT = new Thread(server);
		sT.start();
		while (!server.isTcpReady());
		server.closeTcpService();
		assertEquals(true, server.getTcpPort() > 0);
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
	        returnVal = Integer.parseInt(inputFromServer.readLine());
			inputFromServer.close();
			outputToServer.close();
			clientSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.closeTcpService();
		assertEquals(0, returnVal);
	}
	
	
	/**
	 * Mimics a client by requesting a role from the server instance.
	 * Tests that the first role received is 1.      
	 */
	@Test
	public void firstIsProviderTest(){
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread sT = new Thread(server);
		sT.start();
		Socket clientSocket = null;
		DataOutputStream outputToServer = null;
		BufferedReader inputFromServer = null;
		int isProvider = -1;
		try {
			while (!server.isTcpReady());
			int tcpPort = server.getTcpPort();
			clientSocket = new Socket("localhost", tcpPort);
			outputToServer = 
					new DataOutputStream(clientSocket.getOutputStream());
			inputFromServer = 
					new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        inputFromServer.readLine(); //return value is read first, but not needed
			isProvider = Integer.parseInt(inputFromServer.readLine());
	        inputFromServer.close();
			outputToServer.close();
			clientSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.closeTcpService();
		assertEquals(1, isProvider);
	}
	
	/**
	 * Mimics a clients and requests roles from the server instance.
	 * Tests that the first role received is 1 and the next seven are 0.      
	 */
	@Test
	public void testRoles() throws IOException{
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread serverTh = new Thread(server);
		serverTh.start();
		boolean unexpectedRole = false;
		for (int i = 0; i < AudioStreamServerImpl.MAX_CONNECTIONS; i++){
			Socket clientSocket = null;
			DataOutputStream outputToServer = null;
			BufferedReader inputFromServer = null;
			int isProvider = -1;
			try {
				while (!server.isTcpReady());
				int tcpPort = server.getTcpPort();
				clientSocket = new Socket("localhost", tcpPort);
				outputToServer = new DataOutputStream(clientSocket.getOutputStream());
				inputFromServer = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
		        inputFromServer.readLine(); //return value is read first, but not needed
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
		server.closeTcpService();
		assertEquals(false, unexpectedRole);
	}
	
	/**
	 * Runs max amount of connections in seperate threads
	 * Tests that the num connected is same as max connections.      
	 * @throws InterruptedException 
	 */
	@Test
	public void connectMaxClients() throws InterruptedException{
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread serverTh = new Thread(server);
		serverTh.start();
		
		for (int i = 0; i < AudioStreamServerImpl.MAX_CONNECTIONS; i++){
				AudioStreamClient client = new AudioStreamClientImpl();
				Thread clientTh = new Thread(client);
				clientTh.start();
				Thread.sleep(25);// give client time to connect
		}

		
		int numConnected = server.getNumConnected();
		server.closeTcpService();
		assertEquals(AudioStreamServerImpl.MAX_CONNECTIONS, numConnected);
	}
	
	/**
	 * Runs more than max amount of connections in seperate threads
	 * Tests that the num connected is same as max connections.      
	 */
	@Test
	public void connectOverMaxClients(){
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread serverTh = new Thread(server);
		serverTh.start();
		
		for (int i = 0; i < AudioStreamServerImpl.MAX_CONNECTIONS + 2; i++){
				AudioStreamClient client = new AudioStreamClientImpl();
				Thread clientTh = new Thread(client);
				clientTh.start();
		}
		
		try {
			Thread.sleep(500); // give clients time to connect
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int numConnected = server.getNumConnected();
		server.closeTcpService();
		assertEquals(AudioStreamServerImpl.MAX_CONNECTIONS, numConnected);
	}
	
	/**
	 * Checks that audio can be received from first client, placed into buffer,
	 * and taken out of buffer.
	 * 
	 * @throws InterruptedException 
	 */
	@Test
	public void readWriteToBuffer() throws InterruptedException{
		
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread sT = new Thread(server);
		sT.start();
		
		byte[] data = server.getAudioByte(0);
		String originalObject = data.toString();
		
		AudioStreamClient client = new AudioStreamClientImpl();
		Thread clientTh = new Thread(client);
		clientTh.start();
		
		Thread.sleep(5);
		
		data = server.getAudioByte(0);
		String updatedObject = data.toString();
		
		server.closeTcpService();
		assertEquals(false, originalObject.equals(updatedObject));	
	}
	
}