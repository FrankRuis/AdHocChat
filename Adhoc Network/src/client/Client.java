package client;

import dataobjects.ChatMessage;
import dataobjects.User;
import encryption.DiffieHelman;
import encryption.Encryption;
import utils.ForwardTable;
import utils.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main client class
 *
 * @author Frank
 */
public class Client extends Observable implements Runnable {

	private int port;
	
	private MulticastSocket socket;
	private boolean connected = false;
	
	private ClientSender clientSender;
	private ClientListener clientListener;

	private ForwardTable forwardTable;
	
	private Map<Integer, User> connectedUsers;
	private Map<String, Set<Integer>> destinations;
	private Map<Integer, DiffieHelman> keyPairs;

	private long lastAliveBroadcast;
	
	/**
	 * Constructor
	 */
	public Client(int port) {
		this.port = port;
		this.forwardTable = new ForwardTable(Protocol.getSourceAddress());

		connectedUsers = new ConcurrentHashMap<>();
		destinations = new HashMap<>();
		keyPairs = new HashMap<>();

		destinations.put(Protocol.MAINCHAT, new HashSet<Integer>());

		lastAliveBroadcast = 0;
	}
	
	/**
	 * Connect to the multicast group
	 */
	public void connect() {
		try {
			// Create a multicast socket and join a multicast group
			socket = new MulticastSocket(port);
			socket.joinGroup(Protocol.intAsInetAddress(Protocol.getBroadcastAddress()));
			
			// Create the send and receive buffers
			(clientSender = new ClientSender(20, socket, port, this)).start();
			(clientListener = new ClientListener(20, socket, this)).start();

			// Start the while loop
			connected = true;

			notifyGUI(Protocol.NOTIFY + " Connected.");
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
			clientSender.disconnect();
			clientListener.disconnect();
			connected = false;
			
			// Leave the multicast group and close the socket
			socket.leaveGroup(Protocol.intAsInetAddress(Protocol.getBroadcastAddress()));
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return The forward table
	 */
	public ForwardTable getForwardTable() {
		return forwardTable;
	}

	/**
	 * Get the next hop to reach the given destination
	 * @param destination The destination
	 * @return The next hop
	 */
	public int getNextHop(int destination) {
		return forwardTable.getNextHop(destination);
	}

	/**
	 * Start a diffie helman key exchange
	 * @param destination The destination of the key exchange
	 */
	public void startKeyExchange(int destination) {
		// Generate a public and private key pair for the new user
		DiffieHelman diffieHelman = new DiffieHelman(true);
		keyPairs.put(destination, diffieHelman);

		// Send our public key to the given destination
		sendMessage(Protocol.PUB_KEY + " " + diffieHelman.publicKeyToString(), destination);
	}

	/**
	 * Successfully end the key exchange
	 * @param destination The destination of the exchange
	 */
	public void endKeyExchange(int destination) {
		keyPairs.get(destination).setExchangeSuccesful(true);
	}

	/**
	 * Check if the key exchange has finished
	 * @param destination The destination to check
	 * @return true if the key exchange is finished, else false
	 */
	public boolean isExchanged(int destination) {
		return keyPairs.get(destination).isExchangeSuccesful();
	}

	/**
	 * Decrypt and set the symmetric key for the given destination
	 * @param destination The destination
	 * @param encryptedKey The encrypted symmetric key to set
	 */
	public void addAndDecryptSymmetricKey(int destination, String encryptedKey) {
		DiffieHelman diffieHelman = keyPairs.get(destination);
		diffieHelman.setSymmetricKey(new String(DiffieHelman.decrypt(Encryption.base64Decode(encryptedKey), diffieHelman.getPrivateKey())));

		// Let the other client know we received their key
		sendMessage(Protocol.KEY_RECEIVED, destination);
	}

	/**
	 * Add a symmetric key to a key pair
	 * @param destination The destination using the same key
	 * @param key The key
	 * @return true if the key was added, false if another key exchange is already in progress
	 */
	public boolean addSymmetricKey(int destination, String key) {
		if (!keyPairs.containsKey(destination)) {
			DiffieHelman diffieHelman = new DiffieHelman(false);
			diffieHelman.setSymmetricKey(key);
			keyPairs.put(destination, diffieHelman);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the symmetric key for the given destination
	 * @param destination The destintion
	 * @return The symmetric key
	 */
	public String getSymmetricKey(int destination) {
		return (keyPairs.get(destination) != null && keyPairs.get(destination).isExchangeSuccesful()) ? keyPairs.get(destination).getSymmetricKey() : null;
	}

	/**
	 * Open a connection with the given destination
	 * @param destination The destination to open a conneciton with
	 */
	public void openConnection(int destination) {
		clientSender.openConnection(destination);
		clientListener.openConnection(destination);
	}

	/**
	 * Add a user to the list of connected users
	 * @param user The user to add
	 */
	public void addUser(User user) {
		// If the user does not yet exist
		if (user.getAddress() != Protocol.getSourceAddress() && !connectedUsers.containsKey(user.getAddress())) {
			openConnection(user.getAddress());
			openConnection(user.getAddress());
			destinations.get(Protocol.MAINCHAT).add(user.getAddress());

			// If we don't have a key for this user yet
			if (!keyPairs.containsKey(user.getAddress())) {
				// Start a key exchange
				startKeyExchange(user.getAddress());
			}
		}

		connectedUsers.put(user.getAddress(), user);
	}

	/**
	 * Remove a user from the list of connected users
	 * @param address The user's address
	 */
	public void removeUser(int address) {
		connectedUsers.remove(address);
		clientSender.closeConnection(address);
		clientListener.closeConnection(address);

		if (destinations.get(Protocol.MAINCHAT).contains(address)) {
			destinations.get(Protocol.MAINCHAT).remove(address);
		}
	}

	/**
	 * Remove users that have not been seen for a while
	 */
	public void removeInactiveUsers() {
		for (User user : connectedUsers.values()) {
			// If the last seen timestamp was set more milliseconds ago than the inactivity limit allows
			if (user.getAddress() != Protocol.getSourceAddress() && (System.currentTimeMillis() - user.getLastSeen().getTime()) > Protocol.INACTIVITY_LIMIT) {
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
	 * Acknowledge the given acknowledgement number
	 * @param ack The acknowledgement number to acknowledge
	 * @param source  The source
	 */
	public void acknowledge(int source, int ack) {
		clientSender.acknowledge(source, ack);
	}

	/**
	 * Send an acknowledgement to the given destination
	 * @param destination The destination address
	 * @param ack The acknowledgement number
	 */
	public void sendAck(int destination, int ack) {
		clientSender.sendAck(destination, ack);
	}

	/**
	 * Add a new destination
	 * @param name The destination name
	 * @param addresses The destination addresses (0 or more)
	 */
	public void addDestination(String name, int... addresses) {
		destinations.put(name, new HashSet<Integer>());

		for (int address : addresses) {
			destinations.get(name).add(address);
		}
	}

	/**
	 * Send a ChatMessage object
	 * @param message The ChatMessage object to send
	 */
	public void sendChatMessage(ChatMessage message) {
		for (int address : destinations.get(message.getDestination())) {
			clientSender.sendChatMessage(message, address);
		}
	}

	/**
	 * Send a message
	 * @param message The message to send
	 * @param destination The destination address
	 */
	public void sendMessage(String message, String destination) {
		for (int address : destinations.get(destination)) {
			clientSender.sendMessage(message, address);
		}
	}

	/**
	 * Send a message
	 * @param message The message to send
	 * @param destination The destination address
	 */
	public void sendMessage(String message, int destination) {
		clientSender.sendMessage(message, destination);
	}

	/**
	 * Forward the given packet
	 * @param packet The packet to forward
	 */
	public void forwardPacket(DatagramPacket packet) {
		clientSender.forwardPacket(packet);
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
				clientSender.sendAliveBroadcast(Protocol.ALIVE + " " + connectedUsers.get(Protocol.getSourceAddress()).getName(), Protocol.getBroadcastAddress());
				lastAliveBroadcast = System.currentTimeMillis();
			}

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
