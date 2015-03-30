package client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.LinkedHashMap;
import java.util.Map;

import utils.Protocol;
import dataobjects.ChatMessage;
import dataobjects.Packet;

public class ReceiveBuffer extends Thread {

	private final int WINDOW_SIZE;
	
	private Map<Integer, Packet> buffer;
	
	private MulticastSocket socket;
	private Client client;
	
	private boolean connected = false;
	
	public ReceiveBuffer(int windowSize, MulticastSocket socket, Client client) {
		this.WINDOW_SIZE = windowSize;
		this.buffer = new LinkedHashMap<>();
		this.connected = true;
		this.socket = socket;
		this.client = client;
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
		    client.notifyGUI(message);
		    
		    byteStream.close();
		    objectStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (connected) {
			try {
				DatagramPacket datagramPacket = new DatagramPacket(new byte[Packet.SIZE], Packet.SIZE);
				socket.receive(datagramPacket);
				Packet packet = new Packet(datagramPacket.getData());
				
				if (packet.getSource() != Protocol.SOURCE) {
					if (packet.isFlagSet(Packet.CHATMESSAGE)) {
						receiveChatMessage(packet);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
