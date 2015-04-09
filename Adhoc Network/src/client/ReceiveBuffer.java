package client;

import dataobjects.ChatMessage;
import dataobjects.Packet;
import dataobjects.User;
import utils.Protocol;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReceiveBuffer extends Thread {

	private final int WINDOW_SIZE;
	
	private Map<Integer, Packet> buffer;
	
	private MulticastSocket socket;
	private Client client;
	
	private boolean connected = false;
	
	/**
	 * Constructor
	 * @param windowSize
	 * @param socket
	 * @param client
	 */
	public ReceiveBuffer(int windowSize, MulticastSocket socket, Client client) {
		this.WINDOW_SIZE = windowSize;
		this.buffer = new LinkedHashMap<>();
		this.connected = true;
		this.socket = socket;
		this.client = client;
	}

	/**
	 * Disconnect
	 */
	public void disconnect() {
		connected = false;
	}
	
	/**
	 * Extract a ChatMessage object out of a packet
	 * @param packet
	 */
	public void receiveChatMessage(Packet packet) {
		try {
		    ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getPayload());
		    ObjectInputStream objectStream = new ObjectInputStream(new BufferedInputStream(byteStream));
		    
		    ChatMessage message = (ChatMessage) objectStream.readObject();

			// Add or update the user
			message.getUser().setLastSeen();
			client.addUser(message.getUser());

			// Notify the GUI of the received chat message
		    client.notifyGUI(message);
		    
		    byteStream.close();
		    objectStream.close();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (connected) {
			try {
				DatagramPacket datagramPacket = new DatagramPacket(new byte[Packet.SIZE], Packet.SIZE);
				socket.receive(datagramPacket);
				Packet packet = new Packet(datagramPacket);

				// If the packet was not sent by us
				if (packet.getSource() != Protocol.SOURCE) {
					// If we are the destination
					if (packet.getDestination() == Protocol.BROADCAST || packet.getDestination() == Protocol.SOURCE) {
						// If the payload is a ChatMessage object
						if (packet.isFlagSet(Packet.CHATMESSAGE)) {
							receiveChatMessage(packet);

							// The payload is a command
						} else {
							// Split the command on whitespaces
							String[] command = new String(packet.getPayload()).trim().split("\\s+");

							// Check the command type
							switch (command[0]) {
								// Start a private chat
								case Protocol.PRIVCHAT:
									client.addDestination(command[1], packet.getSource());
									client.notifyGUI(command[0] + " " + command[1]);
									break;
								// Refresh the user's 'alive' status
								case Protocol.ALIVE:
									User user = client.getUser(packet.getSource());

									// If we haven't seen this user before
									if (user == null) {
										// Create a new user and add it to the list of connected users
										User newUser = new User(command[1], null);
										newUser.setAddress(packet.getSource());
										client.addUser(newUser);

										client.notifyGUI(Protocol.NOTIFY + " User " + newUser.getName() + " has entered the chat.");
									} else {
										// Update the user's last seen timestamp
										user.setLastSeen();
									}

									// Forward the alive broadcast
									client.forwardPacket(packet);
									break;
								// Command not known
								default:
									System.err.println("Received an unknown command.");
									break;
							}
						}
					// The packet was not meant for us
					} else {
						// Forward the packet
						client.forwardPacket(packet);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
