package src;

public interface AudioStreamClient extends Runnable {

	/**
	 * Instructs client to connect to server. Receive connection
	 * information and begin participation in audio streaming.
	 * 
	 */
	public void connect();

	/**
	 * Returns id issued by server
	 * 
	 * @return unique id issued by server
	 */
	int getId();

	/**
	 * Returns role in audio stream. True represents provider of audio, while
	 * false indicates the client will be receiving and playing the audio.
	 * 
	 * @return boolean indicating if client is sending audio
	 */
	boolean isAudioProvider();
}
