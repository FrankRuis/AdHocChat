package utils;

import dataobjects.ForwardTableEntry;
import dataobjects.Packet;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

/**
 * Forwarding table class
 *
 * @author Bart
 */
public class ForwardTable {

    private int myAddress;

    private Map<Integer, ForwardTableEntry> forwardTable = new HashMap<>();

    /**
     * Constructor
     * @param myAddress Our own source address
     */
    public ForwardTable(int myAddress) {
        this.myAddress = myAddress;
    }

    /**
     * Add or update an entry in the forwarding table.
     * @param datagramPacket The recevied packet
     */
    public void addEntry(DatagramPacket datagramPacket) {
        // Wrap the packet with our own header
        Packet packet = new Packet(datagramPacket);

        // Get the destination, cost and next hop
        int destination = packet.getSource();
        int cost = Protocol.MAXHOPS - packet.getHops() + 1;
        int nextHop = Protocol.inetAddressAsInt(datagramPacket.getAddress());

        // If the source of the packet was the broadcast address
        if (nextHop == Protocol.getBroadcastAddress() && packet.getHops() == Protocol.MAXHOPS) {
            // Set the next hop to the destination
            nextHop = destination;
        }

        // Create a forwarding table entry
        ForwardTableEntry forwardTableEntry = new ForwardTableEntry(cost, nextHop);

        // If the packet's source is different from ours
        if (packet.getSource() != myAddress) {
            // If the forwarding table does not yet contain this entry, or contains a less optimal entry
            if ((!forwardTable.containsKey(destination)) || (forwardTable.get(destination).getCost() > cost)) {
                forwardTable.put(destination, forwardTableEntry);
            }
        }
    }

    /**
     * Remove the given address from the forwarding table
     * @param address
     */
    public void removeEntry(int address) {
        if (forwardTable.containsKey(address)) {
            forwardTable.remove(address);
        }
    }

    /**
     * Get the next hop to reach the given destination
     * @param destination The destination
     * @return The next hop
     */
    public int getNextHop(int destination) {
        // Check if we have an entry for this destination
        if (forwardTable.containsKey(destination)) {
            // Return the next hop
            return forwardTable.get(destination).getNextHop();
        } else {
            // Return the destination
            return destination;
        }
    }
}
