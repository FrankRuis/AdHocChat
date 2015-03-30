package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Observable;

import dataobjects.ChatMessage;

public class Client extends Observable implements Runnable {
	
	private String address = "228.9.10.11";
	private InetAddress group;
	private int port = 4231;
	
	private MulticastSocket socket;
	private boolean connected = false;
	
	private SendBuffer sendBuffer;
	private ReceiveBuffer receiveBuffer;
	
	/**
	 * Connect to the multicast group
	 */
	public void connect() {
		try {
			socket = new MulticastSocket(port);
			group = InetAddress.getByName(address);
			socket.joinGroup(group);
			
			sendBuffer = new SendBuffer(20, socket, group, port);
			receiveBuffer = new ReceiveBuffer(20, socket, this);
			sendBuffer.start();
			receiveBuffer.start();
			
			connected = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a ChatMessage object
	 * @param message
	 */
	public void sendChatMessage(ChatMessage message) {
		sendBuffer.sendChatMessage(message);
	}
	
	/**
	 * Notify the GUI
	 * @param message The received ChatMessage object
	 */
	public void notifyGUI(ChatMessage message) {
		setChanged();
		notifyObservers(message);
	}

	@Override
	public void run() {
		connect();
		
		while (connected) {
			
		}
	}
}
