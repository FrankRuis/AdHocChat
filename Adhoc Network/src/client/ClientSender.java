package client;

import dataobjects.ChatMessage;
import dataobjects.Packet;
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
	 * @param windowSize
	 * @param socket
	 * @param group
	 * @param port
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
	 * @param destination
	 */
	public void openConnection(int destination) {
		this.openConnections.put(destination, new SendBuffer(WINDOW_SIZE));
	}

	/**
	 * Send a ChatMessage object to the given destination
	 * @param message
	 * @param destination
	 */
	public void sendChatMessage(ChatMessage message, int destination) {
		if (connected) {
			try {
				// Open ByteArray and Object output streams
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				ObjectOutputStream objectStream = new ObjectOutputStream(new BufferedOutputStream(byteStream));

				// Write the ChatMessage object to the object output stream
				objectStream.writeObject(message);
				objectStream.flush();

				// Put the resulting byte array in a packet and set the appropriate flags
				byte[] sendBuffer = byteStream.toByteArray();
				Packet packet = new Packet(sendBuffer.length + Packet.HEADER_SIZE);
				packet.setSource(Protocol.SOURCE);
				packet.setDestination(destination);
				packet.setHops(Protocol.MAXHOPS);
				packet.setFlags(false, true);
				packet.setPayload(sendBuffer);

				// Close the output streams
				byteStream.close();
				objectStream.close();

				// Send the packet
				socket.send(new DatagramPacket(packet.getData(), packet.length(), group, port));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Forward the given packet
	 * @param packet The packet to forward
	 */
	public void forwardPacket(Packet packet) {
		try {
			// Decrease the maximum amount of hops
			packet.decreaseHops();

			// Only forward if the amount of hops is higher than zero
			if (packet.getHops() > 0) {
				socket.send(new DatagramPacket(packet.getData(), packet.length(), group, port));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message to the given destination
	 * @param message
	 * @param destination
	 */
	public void sendMessage(String message, int destination) {
		if (connected) {
			try {
				byte[] sendBuffer = message.getBytes();
				Packet packet = new Packet(sendBuffer.length + Packet.HEADER_SIZE);
				packet.setSource(Protocol.SOURCE);
				packet.setDestination(destination);
				packet.setHops(Protocol.MAXHOPS);
				packet.setFlags(false, false);
				packet.setPayload(sendBuffer);

				socket.send(new DatagramPacket(packet.getData(), packet.length(), group, port));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (connected) {

		}
	}
}
