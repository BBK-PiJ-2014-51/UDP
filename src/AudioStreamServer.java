package src;

public interface AudioStreamServer extends Runnable{

	/**
	 * Returns the current TCP listening port for the next connection.
	 * @return the port to connect to
	 */
	public int getTcpPort();
	
	public boolean isTcpReady();
	
	public void closeTcpService();

	int getNumConnected();

	public void fillBuffer(byte[] data);
	
	public byte[] getAudioByte(int index);

	public int getProviderIndex();

	public void clientReceived(int id);

	public byte[] getNextAudioByte();

	public boolean udpIsReady();

}
