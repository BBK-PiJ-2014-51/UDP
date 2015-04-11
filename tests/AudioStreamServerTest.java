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
	 * Instantiates server with provided tcp port and tests that 
	 * the same port number is returned as the current listening port.
	 */
	@Test
	public void getTcpPort(){
		int tcpPortToOpen = 65501;
		AudioStreamServer server = new AudioStreamServerImpl(65501);
		Thread sT = new Thread(server);
		sT.start();
		int tcpPort = -1;
		do tcpPort = server.getTcpPort();
		while (tcpPort == -1);
		assertEquals(tcpPortToOpen, tcpPort);
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
			int tcpPort = -1;
			do tcpPort = server.getTcpPort();
			while (tcpPort == -1);
			clientSocket = new Socket("localhost", tcpPort);
			outputToServer = 
					new DataOutputStream(clientSocket.getOutputStream());
			inputFromServer = 
					new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outputToServer.writeBytes(String.valueOf(AudioStreamServerImpl.ID_REQUEST));
			outputToServer.writeBytes("\n");
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
	public void connectSeveralClients() throws IOException{
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread[] clientThreads = new Thread[4];
		AudioStreamClient[] clients = new AudioStreamClient[4];
		Set<Integer> expectedIds = new HashSet<Integer>();
		Thread serverTh = new Thread(server);
		serverTh.start();
		
		for (int i = 0; i < 4; i++){
			expectedIds.add(i);
			int tcpPort = -1;
			do tcpPort = server.getTcpPort();
			while (tcpPort == -1);
			clients[i] = new AudioStreamClientImpl(server.getTcpPort());
			clientThreads[i] = new Thread(clients[i]);
			clientThreads[i].start();
			while (clientThreads[i].isAlive());
		}		

		Set<Integer> ids = new HashSet<Integer>();
		for (AudioStreamClient clientInst : clients){
			ids.add(clientInst.getId());
		}
		assertEquals(true, expectedIds.equals(ids));
	}
}