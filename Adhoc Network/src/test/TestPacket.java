package test;

import dataobjects.Packet;

/**
 * @author Frank
 */
public class TestPacket {

	public static void main(String[] args) {
		System.out.println("Creating packet with length 1024");
		Packet packet = new Packet(1024);
		
		System.out.println("Filling packet\n");
		packet.setSource(1);
		packet.setDestination(2);
		packet.setHops((short) 3);
		packet.setSeq(4);
		packet.setAck(5);
		packet.setFlags(true, false, true, true, false, true); // 0b101101 == 45 decimal
		packet.setChecksum((short) 6);
		packet.setPayload("test payload".getBytes());
		packet.setChecksum();
		
		System.out.println("Source: " + packet.getSource());
		System.out.println("Destination: " + packet.getDestination());
		System.out.println("Hops: " + packet.getHops());
		System.out.println("Sequence number: " + packet.getSeq());
		System.out.println("Acknowledgement number: " + packet.getAck());
		System.out.println("Checksum: " + packet.getChecksum() + " - Calculated checksum: " + packet.calculateChecksum());
		System.out.println("Flags decimal value: " + packet.getFlags());
		System.out.println("Flag 1 set: " + packet.isFlagSet(1) + " - Flag 2 set: " + packet.isFlagSet(2) + " - Flag 3 set: " + packet.isFlagSet(3) + " - Flag 4 set: " + packet.isFlagSet(4) + " - Flag 5 set: " + packet.isFlagSet(5) + " - Flag 6 set: " + packet.isFlagSet(6));
		System.out.println("Payload: " + new String(packet.getPayload()).trim());
		
		System.out.println("\nDecreasing hop count\n");
		packet.decreaseHops();
		System.out.println("Hops: " + packet.getHops());
	}
}
