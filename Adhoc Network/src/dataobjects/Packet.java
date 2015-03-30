package dataobjects;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Class for creating and reading the header and data of our custom packet
 * 
 * @author Frank
 */
public class Packet {

	public static final int SIZE = 1024;
	public static final int HEADER_SIZE = 22;
	
	/* Flags */
	public static final int ACK = 1;
	public static final int CHATMESSAGE = 2;
	
	/* Header positions */
	private final int SRC_POS = 0; // Source
	private final int DST_POS = 4; // Destination
	private final int SEQ_POS = 8; // Sequence number
	private final int ACK_POS = 12; // Acknowledgement number
	private final int FLG_POS = 16; // Flags
	private final int HOP_POS = 18; // Hop count
	private final int CSM_POS = 20; // Checksum
	private final int PLD_POS = 22; // Payload
	
	private ByteBuffer buffer;
	
	/**
	 * Constructor for an empty packet
	 * @param size Size of the packet
	 */
	public Packet(int size) {
		buffer = ByteBuffer.allocate(size);
	}
	
	/**
	 * Constructor for a packet with data
	 * @param data
	 */
	public Packet(byte[] data) {
		buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
	}
	
	/**
	 * Set the source address
	 * @param source
	 */
	public void setSource(int source) {
		buffer.putInt(SRC_POS, source);
	}
	
	/**
	 * @return The source address
	 */
	public int getSource() {
		return buffer.getInt(SRC_POS);
	}
	
	/**
	 * Set the destination address
	 * @param destination
	 */
	public void setDestination(int destination) {
		buffer.putInt(DST_POS, destination);
	}
	
	/**
	 * @return The destination address
	 */
	public int getDestination() {
		return buffer.getInt(DST_POS);
	}
	
	/**
	 * Set the sequence number
	 * @param seq
	 */
	public void setSeq(int seq) {
		buffer.putInt(SEQ_POS, seq);
	}
	
	/**
	 * @return The sequence number
	 */
	public int getSeq() {
		return buffer.getInt(SEQ_POS);
	}
	
	/**
	 * Set the acknowledgement number
	 * @param ack
	 */
	public void setAck(int ack) {
		buffer.putInt(ACK_POS, ack);
	}
	
	/**
	 * @return The acknowledgement number
	 */
	public int getAck() {
		return buffer.getInt(ACK_POS);
	}
	
	/**
	 * Set the flags. <br> <br>
	 * <b>Up to 16 flags can be set, starting from the LSB:</b> <br>
	 * 1. ACK <br>
	 * 2. ChatMessage <br>
	 * 
	 * @param flags
	 */
	public void setFlags(short flags) {
		buffer.putShort(FLG_POS, flags);
	}
	
	/**
	 * Set the flags, accepts up to 16 booleans. <br>
	 * First boolean corresponds to the LSB and the first flag. <br> <br>
	 * <b>Flags:</b><br>
	 * 1. ACK <br>
	 * 2. ChatMessage <br>
	 * 
	 * @param flags
	 */
	public void setFlags(boolean... flg) {
		// If any of the flags are set
		if (flg.length > 0) {
			short flags = 0;
			
			// Go through all flags in the array
			for (int n = 0; (n < flg.length) && (n < Short.SIZE - 1); n++) {
				// If the nth flag is set
				if (flg[n]) {
					// Set the nth bit
					flags |= (1 << n);
				} else {
					// Clear the nth bit
					flags &= ~(1 << n);
				}
			}
			
			// Add the flags to the buffer
			buffer.putShort(FLG_POS, flags);
			
		// Write a (short) 0 to the buffer
		} else {
			buffer.putShort(FLG_POS, (short) 0);
		}
	}
	
	/**
	 * Check if the nth flag is set <br> <br>
	 * <b>Flags:</b><br>
	 * 1. ACK <br>
	 * 2. ChatMessage <br>
	 * 
	 * @param n
	 * @return true is the flag is set, else false
	 */
	public boolean isFlagSet(int n) {
		short flags = getFlags();
		
		// Return true if the nth bit is set, else false
		return ((1 << (n - 1)) & flags) > 0;
	}
	
	/**
	 * @return The flags
	 */
	public short getFlags() {
		return buffer.getShort(FLG_POS);
	}
	
	/**
	 * Set the hop count
	 * @param hops
	 */
	public void setHops(short hops) {
		buffer.putShort(HOP_POS, hops);
	}
	
	/**
	 * @return The hop count
	 */
	public int getHops() {
		return buffer.getShort(HOP_POS);
	}
	
	/**
	 * Decrease the hop count by one
	 */
	public void decreaseHops() {
		buffer.putShort(HOP_POS, (short) (buffer.getShort(HOP_POS) - 1));
	}
	
	/**
	 * Set the checksum
	 * @param checksum
	 */
	public void setChecksum(short checksum) {
		buffer.putShort(CSM_POS, checksum);
	}
	
	/**
	 * Calculate and set the checksum
	 */
	public void setChecksum() {
		buffer.putShort(CSM_POS, calculateChecksum());
	}
	
	/**
	 * @return The checksum
	 */
	public short getChecksum() {
		return buffer.getShort(CSM_POS);
	}
	
	/**
	 * @return The payload
	 */
	public byte[] getPayload() {
		return Arrays.copyOfRange(buffer.array(), PLD_POS, buffer.capacity());
	}
	
	/**
	 * Set the payload
	 * @param payload
	 */
	public void setPayload(byte[] payload) {
		buffer.position(PLD_POS);
		buffer.put(payload);
	}
	
	/**
	 * @return The calculated checksum
	 */
	public short calculateChecksum() {
		short checksum = 0;
		
		// Add the 16 bit words to the checksum value
		for (int n = 0; n < buffer.capacity(); n += 2) {
			// Don't add the checksum itself
			if (n != CSM_POS) {
				// Get the nth short and add it to the checksum
				checksum += buffer.getShort(n);
			}
		}
		
		// Perform bitwise complement operation and return the resulting checksum
		return (short) ~checksum;
	}
	
	/**
	 * Set the packet data to the given byte array
	 * @param data
	 */
	public void setData(byte[] data) {
		buffer.clear();
		buffer.put(data);
	}
	
	/**
	 * @return The packet data
	 */
	public byte[] getData() {
		return buffer.array();
	}
	
	/**
	 * @return The length of the packet in bytes
	 */
	public int length() {
		return buffer.capacity();
	}
}
