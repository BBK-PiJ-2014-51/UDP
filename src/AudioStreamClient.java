package src;

public interface AudioStreamClient extends Runnable {

	public boolean connect();

	int getId();

	boolean isAudioProvider();
}
