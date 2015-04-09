package utils;

import dataobjects.Packet;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Send window for ensuring reliable data delivery
 *
 * @author Frank
 */
public class SendBuffer {
    private final int WINDOW_SIZE;

    private Map<Integer, Packet> buffer;

    private int seq;

    /**
     * Constructor
     * @param windowSize The maximum window size
     */
    public SendBuffer(int windowSize) {
        WINDOW_SIZE = windowSize;
        buffer = new LinkedHashMap<>();
        seq = 0;
    }

    /**
     * Add the given packet to the buffer
     * @param packet
     */
    public void addPacket(Packet packet) {
        // If the buffer still has space
        if (buffer.size() < WINDOW_SIZE) {
            // Set the packet's sequence number
            packet.setSeq(seq);

            // Add the packet to the buffer
            buffer.put(seq, packet);

            // Increment the sequence number
            seq += packet.length();

            // Make sure seq does not become negative
            if (seq < 0) {
                seq = 0;
            }
        } else {
            //TODO Send window exceeded
            System.out.println("Send window exceeded");
        }
    }

    /**
     * Acknowledge a packet
     * @param ack The acknowledgement number
     */
    public void ackPacket(int ack) {
        // An acknowledgement also acknowledges all data with a lower sequence number than the acknowledgement number
        for (int seq : buffer.keySet()) {
            if (seq <= ack) {
                buffer.remove(seq);
            }
        }
    }
}
