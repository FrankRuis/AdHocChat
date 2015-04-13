package utils;

import dataobjects.ForwardTableEntry;
import dataobjects.Packet;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bart on 13/04/2015.
 */
public class ForwardTable {

    private int myAddress;

    private Map<Integer, ForwardTableEntry> forwardTable = new HashMap<>();

    public ForwardTable(int myAddress) {
        this.myAddress = myAddress;
    }

    public void addEntry(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);

        int destination = packet.getSource();
        int cost = Protocol.MAXHOPS - packet.getHops() + 1;
        int nextHop = Protocol.inetAddressAsInt(datagramPacket.getAddress());
        ForwardTableEntry fte = new ForwardTableEntry(cost, nextHop);

        // If the packet's source is different from ours
        if (packet.getSource() != myAddress) {
            // If the forwarding table does not yet contain this entry, or contains a less optimal entry
            if ((!forwardTable.containsKey(destination)) || (forwardTable.get(destination).getCost() > cost)) {
                forwardTable.put(destination, fte);
            }
        }
    }

    public int getNextHop(int destination) {
        if (forwardTable.containsKey(destination)) {
            return forwardTable.get(destination).getNextHop();
        } else {
            return destination;
        }
    }
}
