package client;

import dataobjects.ChatMessage;
import dataobjects.Packet;
import utils.Encryption;
import utils.Protocol;
import utils.SendBuffer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Send buffer class, handles sending packets
 *
 * @author Frank
 */
public class ClientSender extends Thread {

	private final int WINDOW_SIZE;
	
	private MulticastSocket socket;

	private Map<Integer, SendBuffer> openConnections;
	
	private boolean connected = false;
	private InetAddress group;
	private int port;

	/**
	 * Constructor
	 * @param windowSize The maximum window size
	 * @param socket The multicast socket
	 * @param group The multicast group
	 * @param port The port number
	 */
	public ClientSender(int windowSize, MulticastSocket socket, InetAddress group, int port) {
		this.socket = socket;
		this.group = group;
		this.port = port;
		WINDOW_SIZE = windowSize;
		connected = true;
		openConnections = new HashMap<>();
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
		openConnections.put(destination, new SendBuffer(WINDOW_SIZE));
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
	 * Acknowledge the given acknowledgement number
	 * @param ack The acknowledgement number to acknowledge
	 * @param source  The source
	 */
	public void acknowledge(int source, int ack) {
		// If the connection is still open
		if (openConnections.containsKey(source)) {
			// Acknowledge the packet
			openConnections.get(source).ackPacket(ack);
		}
	}

	/**
	 * Send an acknowledgement to the given destination
	 * @param destination The destination address
	 * @param ack The acknowledgement number
	 */
	public void sendAck(int destination, int ack) {
		try {
			Packet packet = new Packet(Packet.HEADER_SIZE);
			packet.setSource(Protocol.getSourceAddress());
			packet.setDestination(destination);
			packet.setAck(ack);
			packet.setHops(Protocol.MAXHOPS);
			packet.setFlags(true);
			packet.setLength();
			packet.setChecksum();

			socket.send(new DatagramPacket(packet.getData(), packet.getLength(), group, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a ChatMessage object to the given destination
	 * @param message The ChatMessage object to send
	 * @param destination The destination address
	 */
	public void sendChatMessage(ChatMessage message, int destination) {
		if (connected) {
			try {
				// Check if a connection to the destination is open
				if (openConnections.containsKey(destination)) {
					SendBuffer sendBuffer = openConnections.get(destination);

					// Open ByteArray and Object output streams
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					ObjectOutputStream objectStream = new ObjectOutputStream(new BufferedOutputStream(byteStream));

					// Write the ChatMessage object to the object output stream
					objectStream.writeObject(message);
					objectStream.flush();

					// Put the resulting byte array in a packet and set the appropriate flags
					byte[] buffer = Encryption.encrypt(byteStream.toByteArray());
					Packet packet = new Packet(buffer.length + Packet.HEADER_SIZE);
					packet.setSource(Protocol.getSourceAddress());
					packet.setDestination(destination);
					packet.setHops(Protocol.MAXHOPS);
					packet.setSeq(sendBuffer.getSeq());
					packet.setFlags(false, true);
					packet.setPayload(buffer);
					packet.setLength();
					packet.setChecksum();

					// Close the output streams
					byteStream.close();
					objectStream.close();

					// If we can send a packet, send it and add it to the buffer
					if (sendBuffer.canSend()) {
						socket.send(new DatagramPacket(packet.getData(), packet.getLength(), group, port));
						sendBuffer.addPacket(packet);
					} else {
						System.err.println("Send buffer full.");
					}
				} else {
					System.err.println("Trying to send to an unopened connection");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Forward the given packet
	 * @param datagramPacket The packet to forward
	 */
	public void forwardPacket(DatagramPacket datagramPacket) {
		try {
			Packet packet = new Packet(datagramPacket);

			// Decrease the maximum amount of hops
			packet.decreaseHops();

			// Only forward if the amount of hops is higher than zero
			if (packet.getHops() > 0) {
				packet.setChecksum();
				socket.send(new DatagramPacket(packet.getData(), packet.getLength(), group, port));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message to the given destination
	 * @param message The message to send
	 * @param destination The destination address
	 */
	public void sendMessage(String message, int destination) {
		if (connected) {
			try {
				// Check if a connection to the destination is open
				if (openConnections.containsKey(destination)) {
					SendBuffer sendBuffer = openConnections.get(destination);

					// Build the packet
					byte[] buffer = Encryption.encrypt(message.getBytes());
					Packet packet = new Packet(buffer.length + Packet.HEADER_SIZE);
					packet.setSource(Protocol.getSourceAddress());
					packet.setDestination(destination);
					packet.setHops(Protocol.MAXHOPS);
					packet.setSeq(sendBuffer.getSeq());
					packet.setPayload(buffer);
					packet.setLength();
					packet.setChecksum();

					// If we can send a packet, send it and add it to the buffer
					if (sendBuffer.canSend()) {
						socket.send(new DatagramPacket(packet.getData(), packet.getLength(), group, port));
						sendBuffer.addPacket(packet);
					} else {
						System.err.println("Send buffer full.");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send a message to the given destination
	 * @param message The message to send
	 * @param destination The destination address
	 */
	public void sendAliveBroadcast(String message, int destination) {
		if (connected) {
			try {
				byte[] sendBuffer = Encryption.encrypt(message.getBytes());
				Packet packet = new Packet(sendBuffer.length + Packet.HEADER_SIZE);
				packet.setSource(Protocol.getSourceAddress());
				packet.setDestination(destination);
				packet.setHops(Protocol.MAXHOPS);
				packet.setFlags(false, false);
				packet.setPayload(sendBuffer);
				packet.setLength();
				packet.setChecksum();

				socket.send(new DatagramPacket(packet.getData(), packet.getLength(), group, port));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		// Create a retransmission scheduler
		ScheduledExecutorService retransmitScheduler = Executors.newScheduledThreadPool(1);
		retransmitScheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// Go through all open connections
				for (SendBuffer buffer : openConnections.values()) {
					// Retransmit each unacked packet left in the buffer
					for (Packet packet : buffer.getUnackedPackets().values()) {
						try {
							socket.send(new DatagramPacket(packet.getData(), packet.getLength(), group, port));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, Protocol.TIMEOUT, Protocol.TIMEOUT, TimeUnit.MILLISECONDS);
	}
}
