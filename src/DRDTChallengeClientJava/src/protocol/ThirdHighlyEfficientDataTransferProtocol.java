package protocol;

import java.util.*;

import client.*;

public class ThirdHighlyEfficientDataTransferProtocol extends IRDTProtocol {

    // change the following as you wish:
    private static final int HEADERSIZE = 1;   // number of header bytes in each packet
    private static final int DATASIZE = 220;   // max. number of user data bytes in each packet
    private static final int WINDOWSIZE = 23;
    private static final int DELAYTIME = 8000;
    private static final int SENDTIMER = 5000;
    private static final int NUMBEROFACKCHECKS = 100;
    private static final int DUPLICATEACKS = 3;
    private Map<Integer, Boolean> sendPacketStatus = new HashMap<>();

    @Override
    public void sender() {
        System.out.println("Sending...");

        // read from the input file
        Integer[] fileContents = Utils.getFileContents(getFileID());
        Set<Integer> receivedAcks = new HashSet<>();
        int filePointer;
        int receivedAck = 0;
        int dupAckCounter = 0;

        int numberOfFragments = fileContents.length / DATASIZE + 1;

        System.out.println("Number of packets to send in total = " + numberOfFragments);


        while (receivedAcks.size() != numberOfFragments) {
            // keep track of where we are in the data

            int fragmentCounter;

            int lowerbound;
            int upperbound;

            //define window boundaries
            if (receivedAcks.isEmpty()) {
                lowerbound = 1;
                upperbound = WINDOWSIZE;
            } else {
                lowerbound = determineLowerbound(receivedAcks);
                upperbound = determineUpperbound(receivedAcks, numberOfFragments);
            }

            // create and send a new packet of appropriate size
            for (fragmentCounter = lowerbound; fragmentCounter <= upperbound; fragmentCounter++) {
                if (!receivedAcks.contains(fragmentCounter) && (!sendPacketStatus.containsKey(fragmentCounter) || sendPacketStatus.get(fragmentCounter))) {
                    filePointer = DATASIZE * (fragmentCounter - 1);
                    Integer[] pkt = createPacket(fragmentCounter, fileContents, filePointer);
                    getNetworkLayer().sendPacket(pkt);
                    client.Utils.Timeout.SetTimeout(SENDTIMER, this, pkt[0]);
                    System.out.println("Sent one packet with header=" + pkt[0]);
                }
            }


            receivedAck = checkForAcks();
            if (!receivedAcks.contains(receivedAck)) {
                receivedAcks.add(receivedAck);
                dupAckCounter = 0;
            } else {
                dupAckCounter++;
                if (dupAckCounter == DUPLICATEACKS) {
                    filePointer = DATASIZE * (receivedAck - 1);
                    Integer[] pkt = createPacket(receivedAck, fileContents, filePointer);
                    getNetworkLayer().sendPacket(pkt);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                       System.out.print("dikke snikkel");
                    }
                }
            }
        }
    }

    @Override
    public void receiver() {

        int packetIndex;
        Integer[] fileContents = new Integer[0];
        Map<Integer, Integer[]> packetMap = new HashMap<>();

        System.out.println("Receiving...");

        boolean stop = false;
        while (!stop) {

            Integer[] packet = getNetworkLayer().receivePacket();

            if (packet != null) {

                packetIndex = packet[0];
                System.out.println("Received packet, length=" + packet.length + "  first byte=" + packetIndex);

                if (!packetMap.keySet().contains(packetIndex)) {
                    Integer[] packetData = Arrays.copyOfRange(packet, 1, packet.length);
                    packetMap.put(packetIndex, packetData);
                    sendAck(packetMap);
                }

                if (packetMap.keySet().size() == Collections.max(packetMap.keySet()) && packet.length < DATASIZE) stop = true;

            } else {
                // wait ~10ms (or however long the OS makes us wait) before trying again
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    stop = true;
                }
            }
        }

        int packetPosition = 0;

        for (int index = 1; index <= packetMap.size(); index++) {
            int datalen = packetMap.get(index).length;
            fileContents = Arrays.copyOf(fileContents, packetPosition + datalen);
            System.arraycopy(packetMap.get(index), 0, fileContents, packetPosition, datalen);
            packetPosition = packetPosition + packetMap.get(index).length;
        }

        // write to the output file
        Utils.setFileContents(fileContents, getFileID());
    }

    // create a new packet of appropriate size
    private Integer[] createPacket(int i, Integer[] fileContents, int filePointer) {
        Integer[] pkt = null;
        int datalen = Math.min(DATASIZE, fileContents.length - filePointer);
        if (datalen > -1) {
            pkt = new Integer[HEADERSIZE + datalen];
            pkt[0] = i;
            System.arraycopy(fileContents, filePointer, pkt, HEADERSIZE, datalen);
        }
        return pkt;
    }

    // create a new packet of appropriate size
    private Integer[] createEmptyPacket(int i) {
        return new Integer[]{i};
    }

    private void sendAck(Map<Integer, Integer[]> packetMap) {
        int tobeReceived = Collections.max(packetMap.keySet());
        for (int i = 1; i <= Collections.max(packetMap.keySet()); i++) {
            if (!packetMap.keySet().contains(i)) {
                tobeReceived = i;
                break;
            }
        }
        Integer[] ackPacket = createEmptyPacket(tobeReceived);
        getNetworkLayer().sendPacket(ackPacket);
        System.out.println("ACK for next packet with header = " + tobeReceived);
    }

    // determine lowerbound window size
    private int determineLowerbound(Set<Integer> receivedAcks) {
        return Collections.max(receivedAcks);
    }

    // determine upperbound window size
    private int determineUpperbound(Set<Integer> receivedAcks, int numberOfFragments) {
        int upperbound = determineLowerbound(receivedAcks) + WINDOWSIZE;
        if (upperbound > numberOfFragments) {
            upperbound = numberOfFragments;
        }
        return upperbound;
    }

    @Override
    public void TimeoutElapsed(Object tag) {
        int z = (Integer) tag;
        sendPacketStatus.put(z, true);
//        System.out.println("Timer expired with tag="+z);
    }

    private int checkForAcks() {
        for (int i = 1; i <= NUMBEROFACKCHECKS; i++) {
            try {
                Thread.sleep(DELAYTIME / NUMBEROFACKCHECKS);
                Integer[] ackPacket = getNetworkLayer().receivePacket();
                if (ackPacket != null) {
                    System.out.println("Received ACK packet with header= " + ackPacket[0]);
                    return ackPacket[0];
                }
            } catch (InterruptedException e) {
                System.out.print("JoahJoah");
                break;
            }
        }
        return 0;
    }
}



