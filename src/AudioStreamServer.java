package src;

public interface AudioStreamServer extends Runnable{

	/**
	 * Returns the current TCP listening port for the next connection.
	 * @return the port to connect to
	 */
	public int getTcpPort();
	
	/**
	 * returns status of tcp connection
	 * @return true if ready to receive connections
	 */
	public boolean isTcpReady();
	
	/**
	 * closes tcp port and connect streams
	 */
	public void closeTcpService();

	/**
	 * returns number of current connections
	 * @return number connected
	 */
	int getNumConnected();

	/**
	 * submits a byte array to buffer
	 * @param data to submit
	 */
	public void fillBuffer(byte[] data);
	
	/**
	 * returns a byte array from buffer at requested index
	 * @param index of byte array
	 * @return byte array requested
	 */
	public byte[] getAudioByte(int index);

	/**
	 * index of current provider (will be 0 until refactored)
	 * @return index of audio provider (as opposed to other clients that are listening)
	 */
	public int getProviderIndex();

	/**
	 * registers receipt of last packet
	 * @param id of client notifying receipt
	 */
	public void clientReceived(int id);

	/**
	 * get next audio byte array from buffer
	 * @return next audio bye array
	 */
	public byte[] getNextAudioByte();

	/**
	 * indicates that the next packet is now ready to be streamed from buffer
	 * i.e. all clients have received the last packet
	 * @return true if ready for next
	 */
	public boolean udpIsReady();

}
