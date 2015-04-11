package utils;

import dataobjects.Packet;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Receive buffer for ensuring reliable data transmissions
 *
 * @author Frank
 */
public class ReceiveBuffer {
    private final int WINDOW_SIZE;

    private Set<Integer> buffer;

    /**
     * Constructor
     * @param windowSize The maximum window size
     */
    public ReceiveBuffer(int windowSize) {
        buffer = new LinkedHashSet<>();
        WINDOW_SIZE = windowSize;
    }

    /**
     * Add a packet to the receive buffer
     * @param packet The packet to add
     * @return True if the packet is expected, false if it was duplicate or out of order
     */
    public boolean addPacket(Packet packet) {
        if (!buffer.contains(packet.getSeq())) {
            // Remove the first entry if the buffer is full
            if (buffer.size() >= WINDOW_SIZE) {
                buffer.remove(buffer.iterator().next());
            }

            // Add the packet to the buffer
            buffer.add(packet.getSeq());

            // Return true to signal acceptance of the packet
            return true;
        }

        // Return false to signal rejection of the packet
        return false;
    }
}
