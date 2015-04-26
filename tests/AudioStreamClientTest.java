package tests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.IOException;

import src.AudioStreamClient;
import src.AudioStreamClientImpl;
import src.AudioStreamServer;
import src.AudioStreamServerImpl;

public class AudioStreamClientTest {

	/**
	 * tests that client correctly returns boolean of audio
	 * providing status
	 * @throws InterruptedException
	 */
	@Test
	public void testRoles() throws InterruptedException {
		AudioStreamServer server = new AudioStreamServerImpl();
		Thread sT = new Thread(server);
		sT.start();
		boolean unexpected = false;
		for (int i = 0; i < 2; i++){
			AudioStreamClient client = new AudioStreamClientImpl();
			Thread cT = new Thread(client);
			while (!server.isTcpReady());
			cT.start();
			Thread.sleep(5);
			if (i == 0 && !client.isAudioProvider()) unexpected = true;
			else if (i==1 && client.isAudioProvider()) unexpected = true;
		}
		server.closeTcpService();
		assertEquals(unexpected, false);	
	}
	
	/**
	 * Checks that the first client correctly receives an id, provider status, 
	 * udp port to send audio, and address to send audio. Then sends some audio.
	 * 
	 * Performs check by looking at the first object in the buffer and monitoring it 
	 * for a change.
	 * 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * 
	 */
	@Test
	public void testProvidesAudio() throws IOException, InterruptedException{
		
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
		
		assertEquals(false, originalObject.equals(updatedObject));
	}
}
