package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import dataobjects.ChatMessage;
import dataobjects.User;

public class Client extends Observable implements Runnable {
	
	private String address = "228.9.10.11";
	private InetAddress group;
	private int port = 4231;
	
	private MulticastSocket socket;
	private boolean connected = false;
	
	private SendBuffer sendBuffer;
	private ReceiveBuffer receiveBuffer;
	
	private Map<String, User> connectedUsers;
	
	/**
	 * Constructor
	 */
	public Client() {
		connectedUsers = new HashMap<>();
	}
	
	/**
	 * Connect to the multicast group
	 */
	public void connect() {
		try {
			socket = new MulticastSocket(port);
			group = InetAddress.getByName(address);
			socket.joinGroup(group);
			
			// Create the send and receive buffers
			sendBuffer = new SendBuffer(20, socket, group, port);
			receiveBuffer = new ReceiveBuffer(20, socket, this);
			sendBuffer.start();
			receiveBuffer.start();
			
			// Start the while loop
			connected = true;
			
			notifyGUI("Connected.");
		} catch (IOException e) {
			e.printStackTrace();
			notifyGUI("A problem occurred while connecting.");
		}
	}
	
	/**
	 * Disconnect the socket and leave the multicast group
	 */
	public void disconnect() {
		try {
			// Stop the while loops
			sendBuffer.disconnect();
			receiveBuffer.disconnect();
			connected = false;
			
			// Leave the multicast group and close the socket
			socket.leaveGroup(group);
			socket.close();
			
			notifyGUI("Disconnected.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a user to the list of connected users
	 * @param user
	 */
	public void addUser(User user) {
		connectedUsers.put(user.getName(), user);
	}
	
	/**
	 * Remove a user from the list of connected users
	 * @param name
	 */
	public void removeUser(String name) {
		connectedUsers.remove(name);
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
	 * @param arg An object to send to the GUI
	 */
	public void notifyGUI(Object arg) {
		setChanged();
		notifyObservers(arg);
	}

	@Override
	public void run() {
		connect();

		while (connected) {
			
		}
	}
}
