package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class AudioStreamServerImpl implements AudioStreamServer{

	//config settings
	// TODO move to config file
	public static final int DEFAULT_TCP_PORT = 65000;
	public static final int MAX_CONNECTIONS = 8;
	public static final int BUFFER_SIZE = 65000; //number of bytes in each byte array
	public static final int BUFFER_LENGTH = 512; //number of byte arrays in buffer
	
	private ConnectionHandler[] tcpConns = new ConnectionHandler[MAX_CONNECTIONS]; //client instances
	private int numConnected = 0;
	private Thread[] clientThreads = new Thread[MAX_CONNECTIONS]; //threads for client instances
	private int nextId = 0;
	
	private boolean tcpIsReady = false; //indicates if ready for tcp connection
	private int tcpPort = -1; // current tcp listening port
	private ServerSocket server = null;
	private Socket client = null;
	
	
	private byte[][] buffer = new byte[BUFFER_LENGTH][BUFFER_SIZE];
	private int providerIndex = 0;
	private int nextBufferWrite = 0;

	private boolean[] receivedLast = new boolean[MAX_CONNECTIONS - 1];
	private static int nextBufferRead = 0;
	private int acks = 0;
	
	private boolean udpReady = true; //indicates if all clients have received the last udp data packet
	
	/**
	 * launches run method
	 * @param args
	 */
	public static void main(String[] args) {
		new AudioStreamServerImpl().run();
	}
	
	
	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public int getTcpPort() {
		return tcpPort;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public int getProviderIndex() {
		return providerIndex;
	}
	
	/**
	 * {@inheritDoc}
	 * Stores audio byte array to buffer and increments write index
	 */
	@Override
	public void fillBuffer(byte[] data){
		System.out.println("Writing to buffer at index " + nextBufferWrite);
		buffer[nextBufferWrite++] = data;
		while (nextBufferWrite > (nextBufferRead + 10) % BUFFER_LENGTH ){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (nextBufferWrite == BUFFER_LENGTH)
			nextBufferWrite = 0;
	}
	
	/**
	 * {@inheritDoc}
	 * retrieves audio byte array from buffer by index
	 */
	@Override
	public byte[] getAudioByte(int index){
		System.out.println("Reading from buffer at index " + index);
		return buffer[index];
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTcpReady(){
		return tcpIsReady;
	}
	
	/**
	 * {@inheritDoc}
	 * Starts tcp service
	 */
	@Override
	public void run() {
		try {
			startTcpService();
		} catch (SocketException se){
			System.out.println("Terminating tcp service: " + se.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeTcpService();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumConnected(){
		return numConnected;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clientReceived(int id) {
		acks++;
		System.out.println("client " + id + " received packet");
		checkReceived();
	}
	
	/**
	 * {@inheritDoc}
	 * resets buffer index when at last index.
	 * sends reconnect responses when audio provider disconnects
	 */
	@Override
	public byte[] getNextAudioByte() {
		if (nextBufferRead > nextBufferWrite){
			String response = "reconnec";
			tcpConns[0] = null;
			clientThreads[0] = null;
			nextId = nextBufferRead = nextBufferWrite = numConnected = 0;
			return response.getBytes();
		}
		System.out.println("reading at " + nextBufferRead);
		if (nextBufferRead == BUFFER_LENGTH) nextBufferRead = 0;
		return buffer[nextBufferRead];
		
	}
	
	/**
	 * {@inheritDoc}
	 * closes server and sets tcp to not ready.
	 */
	public void closeTcpService(){
		tcpIsReady = false;
		
		if(server != null)
			try {

				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean udpIsReady() {
		return udpReady;
	}
	
	/**
	 * checks if acknowledgements for current packet equals number of connections.
	 * Does nothing if not, if so the server is now ready for the next packet
	 */
	private void checkReceived(){		
		if (acks == numConnected - 1){
			System.out.println("All received");
			for (int i = 0; i < receivedLast.length; i++){
				receivedLast[i] = false;
			}
			nextBufferRead++;
			udpReady = true;
			acks = 0;
		}
	}
	
	/**
	 * binds server to default port and begins accept connections.
	 * server will wait once max connections are reached.
	 * 
	 * @throws IOException
	 * @throws SocketException
	 */
	private void startTcpService() throws IOException, SocketException{
		if (server == null){
			server = new ServerSocket(DEFAULT_TCP_PORT);
			tcpPort = server.getLocalPort();
		}	
		
		tcpIsReady = true;
		while(numConnected < MAX_CONNECTIONS){
			client = server.accept();
			tcpConns[numConnected] = new ConnectionHandler(nextId++, client, this);
			clientThreads[numConnected] = new Thread(tcpConns[numConnected]);
			clientThreads[numConnected++].start();				
		}
		
		tcpIsReady = false;
		while (numConnected >= MAX_CONNECTIONS){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		startTcpService();
	}
}	 