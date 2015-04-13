package client;

import dataobjects.ChatMessage;
import dataobjects.Packet;
import dataobjects.User;
import encryption.DiffieHelman;
import encryption.Encryption;
import utils.Protocol;
import utils.ReceiveBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Receive buffer class, handles receiving packets
 *
 * @author Frank
 */
public class ClientListener extends Thread {

	private final int WINDOW_SIZE;

	private Map<Integer, ReceiveBuffer> openConnections;
	private ReceiveBuffer forwardBuffer;
	
	private MulticastSocket socket;
	private Client client;
	
	private boolean connected = false;
	
	/**
	 * Constructor
	 * @param windowSize The maximum window size for the send windows
	 * @param socket The multicast socket
	 * @param client The client object
	 */
	public ClientListener(int windowSize, MulticastSocket socket, Client client) {
		this.socket = socket;
		this.client = client;
		WINDOW_SIZE = windowSize;
		openConnections = new LinkedHashMap<>();
		forwardBuffer = new ReceiveBuffer(WINDOW_SIZE);
		connected = true;
	}

	/**
	 * Disconnect
	 */
	public void disconnect() {
		connected = false;
	}

	/**
	 * Open a connection with the given destination
	 * @param destination The destination of the connection
	 */
	public void openConnection(int destination) {
		if (!openConnections.containsKey(destination)) {
			openConnections.put(destination, new ReceiveBuffer(WINDOW_SIZE));
		}
	}

	/**
	 * Close the connection with the given destination
	 * @param destination The destination of the connection
	 */
	public void closeConnection(int destination) {
		// Check if the connection exists
		if (openConnections.containsKey(destination)) {
			openConnections.remove(destination);
		}
	}

	/**
	 * Extract a ChatMessage object out of a packet
	 * @param packet The packet containing the ChatMessage object
	 */
	public void receiveChatMessage(Packet packet) {
		try {
			// ByteArray and Object input streams to read the object stored in the packet payload
		    ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getPayload());
		    ObjectInputStream objectStream = new ObjectInputStream(new BufferedInputStream(byteStream));

			// Read the ChatMessage object
		    ChatMessage message = (ChatMessage) objectStream.readObject();

			// Add or update the user
			message.getUser().setLastSeen();
			client.addUser(message.getUser());

			// Notify the GUI of the received chat message
		    client.notifyGUI(message);

			// Close the input streams
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
				// Try to receive a packet
				DatagramPacket datagramPacket = new DatagramPacket(new byte[Packet.SIZE], Packet.SIZE);
				socket.receive(datagramPacket);
				Packet packet = new Packet(datagramPacket);

				// Check the checksum
				if (packet.getChecksum() == packet.calculateChecksum()) {
					// If the packet was not sent by us
					if (packet.getSource() != Protocol.getSourceAddress() && Protocol.inetAddressAsInt(datagramPacket.getAddress()) != Protocol.getSourceAddress()) {
						// If we are the destination
						if (packet.getDestination() == Protocol.BROADCAST || packet.getDestination() == Protocol.getSourceAddress()) {
							// If the packet is encrypted
							if (packet.isFlagSet(Packet.ENCRYPTION) && packet.isFlagSet(Packet.KEYEXCHANGED)) {
								// End the key exchange
								client.endKeyExchange(packet.getSource());

								// If possible, decrypt the packet with the symmetric key from a key exchange
								byte[] decryptedPayload = Encryption.decrypt(packet.getPayload(), client.getSymmetricKey(packet.getSource()));
								packet.setPayload(decryptedPayload);
								packet.setLength(Packet.HEADER_SIZE + decryptedPayload.length);

							// Packets with the broadcast address as the destination should be decrypted with the standard key
							} else if (packet.isFlagSet(Packet.ENCRYPTION)) {
								// Decrypt the packet
								byte[] decryptedPayload = Encryption.decrypt(packet.getPayload(), null);
								packet.setPayload(decryptedPayload);
								packet.setLength(Packet.HEADER_SIZE + decryptedPayload.length);
							}

							// If it is an acknowledgement
							if (packet.isFlagSet(Packet.ACK)) {
								// Handle the acknowledgement
								client.acknowledge(packet.getSource(), packet.getAck());

							// If the payload is a ChatMessage object
							} else if (packet.isFlagSet(Packet.CHATMESSAGE)) {
								// If the connection is open and the packet is accepted
								if (openConnections.containsKey(packet.getSource()) && openConnections.get(packet.getSource()).addPacket(packet)) {
									// Parse the ChatMessage object
									receiveChatMessage(packet);
								}

								// Acknowledge the received packet
								client.sendAck(packet.getSource(), packet.getSeq() + 1);

							// The payload is a command
							} else {
								// Split the command on whitespaces
								String[] command = new String(packet.getPayload()).split("\\s+");

								// Check the command type
								switch (command[0]) {
									// Start a private chat
									case Protocol.PRIVCHAT:
										// If the connection is open and the packet is accepted
										if (openConnections.containsKey(packet.getSource()) && openConnections.get(packet.getSource()).addPacket(packet)) {
											// Start the private chat
											client.addDestination(command[1], packet.getSource());
											client.notifyGUI(command[0] + " " + command[1]);
										}

										// Acknowledge the received packet
										client.sendAck(packet.getSource(), packet.getSeq() + 1);
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
										client.forwardPacket(datagramPacket);
										break;

									// Someone changed their name
									case Protocol.NAME_CHANGE:
										// If the connection is open and the packet is accepted
										if (openConnections.containsKey(packet.getSource()) && openConnections.get(packet.getSource()).addPacket(packet)) {
											client.notifyGUI(Protocol.NOTIFY + " User " + command[1] + " changed their name to " + command[2] + ".");
											client.getUser(packet.getSource()).setName(command[2]);
										}

										// Acknowledge the received packet
										client.sendAck(packet.getSource(), packet.getSeq() + 1);
										break;

									// We have received a public key
									case Protocol.PUB_KEY:
										// Open a connection with the packet sender
										client.openConnection(packet.getSource());

										// Acknowledge the received packet
										client.sendAck(packet.getSource(), packet.getSeq() + 1);

										// If the connection is open and the packet is accepted
										if (openConnections.containsKey(packet.getSource()) && openConnections.get(packet.getSource()).addPacket(packet)) {
											String generatedKey = Encryption.generateKey();

											// Check if we aren't already exchanging keys with this user
											if (client.addSymmetricKey(packet.getSource(), generatedKey)) {
												client.sendMessage(Protocol.SYM_KEY + " " + Encryption.base64Encode(DiffieHelman.encrypt(generatedKey.getBytes(), DiffieHelman.stringToPublicKey(new String(packet.getPayload()).split("\\s+", 2)[1]))), packet.getSource());
											}
										}
										break;

									// We have received a symmetric key
									case Protocol.SYM_KEY:
										// Acknowledge the received packet
										client.sendAck(packet.getSource(), packet.getSeq() + 1);

										// If the connection is open and the packet is accepted
										if (openConnections.containsKey(packet.getSource()) && openConnections.get(packet.getSource()).addPacket(packet)) {
											client.addAndDecryptSymmetricKey(packet.getSource(), new String(packet.getPayload()).split("\\s+", 2)[1]);
										}
										break;

									// Successfully exchanged symmetric keys for encryption
									case Protocol.KEY_RECEIVED:
										// Acknowledge the received packet
										client.sendAck(packet.getSource(), packet.getSeq() + 1);

										// If the connection is open and the packet is accepted
										if (openConnections.containsKey(packet.getSource()) && openConnections.get(packet.getSource()).addPacket(packet)) {
											client.endKeyExchange(packet.getSource());
										}
										break;

									// Command not known
									default:
										System.err.println("Received an unknown command.");
										break;
								}
							}

						// The packet was not meant for us
						} else {
							// If we haven't forwarded this packet yet
							if (forwardBuffer.addPacket(packet)) {
								// Forward the packet
								client.forwardPacket(datagramPacket);
							}
						}
					}
				} else {
					System.err.println("Wrong checksum.");
				}
			} catch (IOException e) {
				// Ignore the exception if it was expected
				if (!socket.isClosed()) {
					e.printStackTrace();
					client.notifyGUI(Protocol.NOTIFY + " Connection lost.");
				}
			}
		}
	}
}
