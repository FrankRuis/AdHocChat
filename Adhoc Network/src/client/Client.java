package client;

import dataobjects.ChatMessage;
import dataobjects.User;
import utils.Protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class Client extends Observable implements Runnable {
	
	private String address;
	private InetAddress group;
	private int port;
	
	private MulticastSocket socket;
	private boolean connected = false;
	
	private SendBuffer sendBuffer;
	private ReceiveBuffer receiveBuffer;
	
	private Map<String, User> connectedUsers;
	private Map<String, Set<Integer>> destinations;
	
	/**
	 * Constructor
	 */
	public Client(String address, int port) {
		this.address = address;
		this.port = port;
		connectedUsers = new HashMap<>();
		destinations = new HashMap<>();
		destinations.put(Protocol.MAINCHAT, new HashSet<Integer>());
		destinations.get(Protocol.MAINCHAT).add(Protocol.BROADCAST);
	}
	
	/**
	 * Connect to the multicast group
	 */
	public void connect() {
		try {
			// Create a multicast socket and join a multicast group
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

			notifyGUI(Protocol.NOTIFY  + " Connected.");
		} catch (IOException e) {
			e.printStackTrace();
			notifyGUI(Protocol.NOTIFY + " A problem occurred while connecting.");
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
			
			notifyGUI(Protocol.NOTIFY  + " Disconnected.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a user to the list of connected users
	 * @param user
	 */
	public void addUser(User user) {
		// If the username does not yet exist
		if (!connectedUsers.containsKey(user.getName())) {
			connectedUsers.put(user.getName(), user);
		} else {
			// TODO Username exists
		}
	}
	
	/**
	 * Remove a user from the list of connected users
	 * @param name
	 */
	public void removeUser(String name) {
		connectedUsers.remove(name);
	}

	/**
	 * Get the user with the given name
	 * @param name The name of the user to get
	 * @return The user object
	 */
	public User getUser(String name) {
		return connectedUsers.get(name);
	}

	/**
	 * Send a ChatMessage object
	 * @param message
	 */
	public void sendChatMessage(ChatMessage message) {
		for (int address : destinations.get(message.getDestination())) {
			sendBuffer.sendChatMessage(message, address);
		}
	}

	/**
	 * Add a new destination
	 * @param name
	 * @param privateChat
	 */
	public void addDestination(String name, boolean privateChat) {
		destinations.put(name, new HashSet<Integer>());

		if (privateChat) {
			destinations.get(name).add(connectedUsers.get(name).getAddress());
		}
	}

	/**
	 * Send a message
	 * @param message
	 * @param destination
	 */
	public void sendMessage(String message, String destination) {
		for (int address : destinations.get(destination)) {
			sendBuffer.sendMessage(message, address);
		}
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
	}
}
