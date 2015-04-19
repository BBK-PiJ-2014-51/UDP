package tests;

import static org.junit.Assert.*;

import org.junit.Test;

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
		assertEquals(unexpected, false);	
	}
}
