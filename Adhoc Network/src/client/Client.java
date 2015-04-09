package client;

import dataobjects.ChatMessage;
import dataobjects.Packet;
import dataobjects.User;
import utils.Protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Client extends Observable implements Runnable {
	
	private String address;
	private InetAddress group;
	private int port;
	
	private MulticastSocket socket;
	private boolean connected = false;
	
	private SendBuffer sendBuffer;
	private ReceiveBuffer receiveBuffer;
	
	private Map<Integer, User> connectedUsers;
	private Map<String, Set<Integer>> destinations;

	private long lastAliveBroadcast;
	
	/**
	 * Constructor
	 */
	public Client(String address, int port) {
		this.address = address;
		this.port = port;

		connectedUsers = new ConcurrentHashMap<>();
		destinations = new HashMap<>();

		destinations.put(Protocol.MAINCHAT, new HashSet<Integer>());
		destinations.get(Protocol.MAINCHAT).add(Protocol.BROADCAST);

		lastAliveBroadcast = 0;
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
		connectedUsers.put(user.getAddress(), user);
	}
	
	/**
	 * Remove a user from the list of connected users
	 * @param address
	 */
	public void removeUser(int address) {
		connectedUsers.remove(address);
	}


	/**
	 * Remove users that have not been seen for a while
	 */
	public void removeInactiveUsers() {
		for (User user : connectedUsers.values()) {
			// If the last seen timestamp was set more milliseconds ago than the inactivity limit allows
			if (user.getAddress() != Protocol.SOURCE && (System.currentTimeMillis() - user.getLastSeen().getTime()) > Protocol.INACTIVITY_LIMIT) {
				// Remove the user
				this.removeUser(user.getAddress());
				notifyGUI(Protocol.PART + " " + user.getName());
			}
		}
	}

	/**
	 * Get the user with the given name
	 * @param address The address of the user to get
	 * @return The user object
	 */
	public User getUser(int address) {
		return connectedUsers.get(address);
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
	 * @param addresses
	 */
	public void addDestination(String name, int... addresses) {
		destinations.put(name, new HashSet<Integer>());

		for (int address : addresses) {
			destinations.get(name).add(address);
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
	 * Forward the given packet
	 * @param packet The packet to forward
	 */
	public void forwardPacket(Packet packet) {
		sendBuffer.forwardPacket(packet);
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

		while (connected){
			// Check if we should send an 'alive' broadcast
			if (System.currentTimeMillis() - lastAliveBroadcast > Protocol.ALIVE_RATE) {
				// Remove inactive users
				removeInactiveUsers();

				// Send an 'alive' broadcast to let others know we're here
				sendBuffer.sendMessage(Protocol.ALIVE + " " + connectedUsers.get(Protocol.SOURCE).getName(), Protocol.BROADCAST);
				lastAliveBroadcast = System.currentTimeMillis();
			}
		}
	}
}
