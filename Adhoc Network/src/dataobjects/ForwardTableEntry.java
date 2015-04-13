package dataobjects;

/**
 * Created by Bart on 13/04/2015.
 */
public class ForwardTableEntry {

    private int cost;
    private int nextHop;

    public ForwardTableEntry(int cost, int nextHop) {
        this.cost = cost;
        this.nextHop = nextHop;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getNextHop() {
        return nextHop;
    }

    public void setNextHop(int nextHop) {
        this.nextHop = nextHop;
    }
}
