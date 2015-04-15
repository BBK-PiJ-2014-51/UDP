package src;

public interface AudioStreamServer extends Runnable{

	/**
	 * Returns the current TCP listening port for the next connection.
	 * @return the port to connect to
	 */
	public int getTcpPort();
	
	public boolean isTcpReady();

}
