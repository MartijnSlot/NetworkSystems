package protocol;

import client.*;

import java.util.HashMap;
import java.util.Map;

public class DummyRoutingProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;

    // You can use this data structure to store your forwarding table with extra information.
    private HashMap<Integer, DummyRoute> myForwardingTable = new HashMap<>();

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
    }


    @Override
    public void tick(Packet[] packets) {
        // Get the address of this node
        int myAddress = this.linkLayer.getOwnAddress();

        System.out.println("tick; received " + packets.length + " packets");
        int i;

        // first process the incoming packets; loop over them:
        for (i = 0; i < packets.length; i++) {
            int neighbour = packets[i].getSourceAddress();          // from whom is the packet?
            int linkcost = this.linkLayer.getLinkCost(neighbour);   // what's the link cost from/to this neighbour?
            DataTable dt = packets[i].getDataTable();                    // other data contained in the packet
            System.out.printf("received packet from %d with %d rows and %d columns of data%n", neighbour, dt.getNRows(), dt.getNColumns());

            // you'll probably want to process the data, update your data structures (myForwardingTable) , etc....

            // reading one cell from the DataTable can be done using the  dt.get(row,column)  method

           /* example code for inserting a route into myForwardingTable:
               DummyRoute r = new DummyRoute();
               r.nextHop = ...someneighbour...;
               myForwardingTable.put(...somedestination... , r);
           */

//// example code for checking whether some destination is already in myForwardingTable, and accessing it:
///              if (myForwardingTable.containsKey(dest)) {
//                   DummyRoute r = myForwardingTable.get(dest);
//               }


        }

        // and send out one (or more, if you want) distance vector packets
        // the actual distance vector data must be stored in the DataTable structure
        DataTable dt = new DataTable(6);   // the 6 is the number of columns, you can change this
        // you'll probably want to put some useful information into dt here
        // by using the  dt.set(row, column, value)  method.

        // next, actually send out the packet, with our own address as the source address
        // and 0 as the destination address: that's a broadcast to be received by all neighbours.
        Packet pkt = new Packet(myAddress, 0, dt);
        this.linkLayer.transmit(pkt);

        /*
        Instead of using Packet with a DataTable you may also use Packet with
        a byte[] as data part, if really you want to send your own data structure yourself.
        Read the JavaDoc of Packet to see how you can do this.
        PLEASE NOTE! Although we provide this option we do not support it.
        */
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
}
