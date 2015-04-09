package client;

import dataobjects.ChatMessage;
import dataobjects.Packet;
import utils.Protocol;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SendBuffer extends Thread {

	private final int WINDOW_SIZE;
	
	private Map<Integer, Packet> buffer;
	private List<Integer> acknowledgements;
	
	private MulticastSocket socket;
	
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
	public SendBuffer(int windowSize, MulticastSocket socket, InetAddress group, int port) {
		this.socket = socket;
		this.WINDOW_SIZE = windowSize;
		this.buffer = new LinkedHashMap<>();
		this.acknowledgements = new LinkedList<>();
		this.group = group;
		this.port = port;
		this.connected = true;
	}
	
	/**
	 * Disconnect
	 */
	public void disconnect() {
		connected = false;
	}

	/**
	 * Send a ChatMessage object to the given destination
	 * @param message
	 * @param destination
	 */
	public void sendChatMessage(ChatMessage message, int destination) {
		if (connected) {
			try {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				ObjectOutputStream objectStream = new ObjectOutputStream(new BufferedOutputStream(byteStream));
	
				objectStream.writeObject(message);
				objectStream.flush();
				
				byte[] sendBuffer = byteStream.toByteArray();
				Packet packet = new Packet(sendBuffer.length + Packet.HEADER_SIZE);
				packet.setSource(Protocol.SOURCE);
				packet.setDestination(destination);
				packet.setHops(Protocol.MAXHOPS);
				packet.setFlags(false, true);
				packet.setPayload(sendBuffer);
				
				byteStream.close();
				objectStream.close();
				
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
