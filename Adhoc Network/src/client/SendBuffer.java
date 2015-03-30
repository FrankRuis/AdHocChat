package client;

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

import utils.Protocol;
import dataobjects.ChatMessage;
import dataobjects.Packet;

public class SendBuffer extends Thread {

	private final int WINDOW_SIZE;
	
	private Map<Integer, Packet> buffer;
	private List<Integer> acknowledgements;
	
	private MulticastSocket socket;
	
	private boolean connected = false;
	private InetAddress group;
	private int port;
	
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
	 * Send a ChatMessage object
	 * @param message
	 */
	public void sendChatMessage(ChatMessage message) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(new BufferedOutputStream(byteStream));

			objectStream.writeObject(message);
			objectStream.flush();
			
			byte[] sendBuffer = byteStream.toByteArray();
			Packet packet = new Packet(sendBuffer.length + Packet.HEADER_SIZE);
			packet.setSource(Protocol.SOURCE);
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
	
	@Override
	public void run() {
		while (connected) {

		}
	}
}
