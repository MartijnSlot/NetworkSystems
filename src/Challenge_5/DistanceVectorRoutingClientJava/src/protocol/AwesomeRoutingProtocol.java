package protocol;

import client.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AwesomeRoutingProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;
    private int myAddress;

    // You can use this data structure to store your forwarding table with extra information.
    private HashMap<Integer, DummyRoute> myForwardingTable = new HashMap<>();

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
        myAddress = this.linkLayer.getOwnAddress();
        System.out.print("My address: " + myAddress +"\n");
        DataTable InitDt = new DataTable(3);
        InitDt = fillDateTable(myAddress,0,-1,0, InitDt);
        Packet pkt = new Packet(myAddress, 0, InitDt);
        this.linkLayer.transmit(pkt);
    }


    @Override
    public void tick(Packet[] packets) {
        Set<Integer> setToRemove = new HashSet<>();

        // Get the address of this node
        System.out.println("tick; received " + packets.length + " packets");

        // first process the incoming packets; loop over them:
        for (Packet packet : packets) {
            int neighbour = packet.getSourceAddress();          // from whom is the packet?
            int linkcost = this.linkLayer.getLinkCost(neighbour);   // what's the link cost from/to this neighbour?
            DataTable IncomingDT = packet.getDataTable();           // other data contained in the packet
            System.out.printf("received packet from %d with %d rows and %d columns of data%n", neighbour, IncomingDT.getNRows(), IncomingDT.getNColumns());

            for (int row = 0; row < IncomingDT.getNRows(); row++) {
                DummyRoute r = new DummyRoute();

                r.nextHop = neighbour;
                r.cost = (linkcost + IncomingDT.get(row, 1));
                int dest = IncomingDT.get(row, 0);
                Set<Integer> neighbours = determineNeighbours();

                if (r.cost == -1) {
                    for (int destToRemove : myForwardingTable.keySet()) {
                        if(myForwardingTable.get(destToRemove).nextHop == neighbour) {
                            setToRemove.add(destToRemove);
                            for (int noaber : neighbours) {
                                if (myForwardingTable.get(noaber).cost != -1) {
                                    DataTable emptyDT = new DataTable(3);
                                    emptyDT = fillDateTable(myAddress, -1, noaber, 0, emptyDT);
                                    Packet pkt = new Packet(myAddress, 0, emptyDT);
                                    this.linkLayer.transmit(pkt);
                                }
                            }
                        }
                    }
                }

                if ((!myForwardingTable.containsKey(dest) || r.cost < myForwardingTable.get(dest).cost)) {
                    myForwardingTable.put(dest, r);
                    System.out.println("New route added to forwarding table: \nDestination: "
                            + dest + " Cost: " + linkcost + " Neighbour: " + neighbour);
                }
            }
        }

        for (int remover : setToRemove) {
            myForwardingTable.remove(remover);
            System.out.println("Removed Destination: " + remover);

        }

        int row = 0;
        DataTable OutgoingDT = new DataTable(3);
        for (int dest : myForwardingTable.keySet()) {
            DummyRoute dr = myForwardingTable.get(dest);
            OutgoingDT = fillDateTable(dest, dr.cost, dr.nextHop, row, OutgoingDT);
            row++;
        }

        // next, actually send out the packet, with our own address as the source address
        // and 0 as the destination address: that's a broadcast to be received by all neighbours.
        Packet pkt = new Packet(myAddress, 0, OutgoingDT);
        this.linkLayer.transmit(pkt);

    }

    private Set<Integer> determineNeighbours() {
        Set<Integer> neighbours = new HashSet<>();
        if (!myForwardingTable.isEmpty()) {
            for (int noaber : myForwardingTable.keySet()) {
                if (myForwardingTable.get(noaber).cost != -1) {
                    neighbours.add(noaber);
                }
            }
        }

        return neighbours;
    }

    public HashMap<Integer, Integer> getForwardingTable() {
        // This code transforms your forwarding table which may contain extra information
        // to a simple one with only a next hop (value) for each destination (key).
        // The result of this method is send to the server to validate and score your protocol.

        // <Destination, NextHop>
        HashMap<Integer, Integer> ft = new HashMap<>();

        for (Map.Entry<Integer, DummyRoute> entry : myForwardingTable.entrySet()) {
            ft.put(entry.getKey(), entry.getValue().nextHop);
        }

        return ft;
    }

    private DataTable fillDateTable(int dest, int cost , int neigh, int row, DataTable dt) {
        dt.set(row, 0, dest);
        dt.set(row, 1, cost);
        dt.set(row, 2, neigh);
        return dt;
    }
}
