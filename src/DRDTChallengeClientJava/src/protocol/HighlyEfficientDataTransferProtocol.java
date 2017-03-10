package protocol;

import java.util.*;

import client.*;

public class HighlyEfficientDataTransferProtocol extends IRDTProtocol {

    // change the following as you wish:
    private static final int HEADERSIZE = 1;   // number of header bytes in each packet
    private static final int DATASIZE = 256;   // max. number of user data bytes in each packet
    private static final int TIMEOUT = 8000;
    private static final int TIMES_TO_CHECK_FOR_ACK = 100;
    private static final int WINDOW_SIZE = 3;

    @Override
    public void sender() {
        System.out.println("Sending...");

        // read from the input file
        Integer[] fileContents = Utils.getFileContents(getFileID());

        // keep track of where we are in the data
        int filePointer = 0;
        int highestReceivedAck = 0;
        int numPackets = fileContents.length / DATASIZE + 1;
        int ackCounter;
        int packetCounter;


        //send packets
        while (filePointer < fileContents.length) {

            System.out.print("Number of packets to send: " + numPackets + "\n");

            for (packetCounter = 1; packetCounter <= numPackets; packetCounter++) {

                int upperBound = packetCounter + WINDOW_SIZE;
                if (upperBound > numPackets) {
                    upperBound = numPackets + 1;
                }

                for (int i = packetCounter; i < upperBound; i++) {
                    Integer[] packetFragment = createPacket(i, fileContents, filePointer);
                    getNetworkLayer().sendPacket(packetFragment);

                    if (packetFragment != null) {
                        // send the packetFragment to the network layer
                        getNetworkLayer().sendPacket(packetFragment);
                        System.out.println("Sent one packet with header= " + packetFragment[0]);
                    }

                    filePointer = (DATASIZE * i);
                }

                // check the highest received ACK
                ackCounter = numberOfReceivedAck();
                if (ackCounter > highestReceivedAck) {
                    highestReceivedAck = ackCounter;
                }

                if (highestReceivedAck < packetCounter && highestReceivedAck != 0) {
                    packetCounter = highestReceivedAck;
                }

                filePointer = packetCounter * DATASIZE;
            }

        }
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

    //check for acks TIMES_TO_CHECK_FOR_ACK times within the timeOUT.
    private int numberOfReceivedAck() {
        int ackCounter;

        for (int i = 0; i <= TIMES_TO_CHECK_FOR_ACK; i++) {
            try {
                Thread.sleep(TIMEOUT / TIMES_TO_CHECK_FOR_ACK);
                Integer[] ackPacket = getNetworkLayer().receivePacket();
                if (ackPacket != null) {
                    ackCounter = ackPacket[0];
                    System.out.print("received ack for packet with header= " + ackCounter + "\n");
                    return ackCounter;
                }
            } catch (InterruptedException e) {
                return 0;
            }
        }
        return 0;
    }

    private boolean previousAcksReceived(Integer[] packet, Set<Integer> receivedPackets) {
        for (int i = 1; i < packet[0]; i++) {
            if (!receivedPackets.contains(i)) {
                return false;
            }
        }
        return true;
    }

    // create a new packet of appropriate size
    private Integer[] createEmptyPacket(int i) {
        return new Integer[]{i};
    }

    @Override
    public void TimeoutElapsed(Object tag) {
        int z = (Integer) tag;
        // handle expiration of the timeout:
        System.out.println("Timer expired with tag=" + z);
    }
    //TODO netjes maken en acks terugsturen binnen de window die daarna niet meer verstuurd worden.
    @Override
    public void receiver() {
        System.out.println("Receiving...");

        Integer[] fileContents = new Integer[0];
        Set<Integer> receivedPackets = new HashSet<>();
        boolean stop = false;

        while (!stop) {

            Integer[] packet = getNetworkLayer().receivePacket();
            Integer[] prematurePacket = new Integer[0];

            if (packet != null) {

                if (receivedPackets.size() > 0) {
                    int j = Collections.max(receivedPackets);
                    if (packet[0] - j > 1) {
                        prematurePacket = packet;
                        System.out.print("Packet " + packet[0] +
                                " arrived too early. First wait for packet " + (packet[0] - 1) + "\n");
                    }
                }

                System.out.println("Received packet, length=" + packet.length + "  first byte=" + packet[0]);

                /**
                 * send ACK packet with the right header back if all previous packets are received
                 */
                if (previousPacketsReceived(packet, receivedPackets) && !receivedPackets.contains(packet[0])) {
                    int oldlength = fileContents.length;
                    int datalen = packet.length - HEADERSIZE;

                    sendAck(packet);
                    fileContents = Arrays.copyOf(fileContents, oldlength + datalen);
                    System.arraycopy(packet, HEADERSIZE, fileContents, oldlength, datalen);
                    System.out.print("Packet " + packet[0] +
                            " has been added to the file on spot " + oldlength +
                            " to " + (oldlength + datalen) + "\n");
                    receivedPackets.add(packet[0]);
                    if (packet.length < DATASIZE) {
                        stop = true;
                    }
                    if (prematurePacket.length > 0) {
                        sendAck(prematurePacket);
                        oldlength = fileContents.length;
                        datalen = packet.length - HEADERSIZE;
                        fileContents = Arrays.copyOf(fileContents, oldlength + datalen);
                        System.arraycopy(packet, HEADERSIZE, fileContents, oldlength, datalen);
                        System.out.print("Packet " + packet[0] +
                                " has been added to the file on spot " + oldlength +
                                " to " + (oldlength + datalen) + "\n");
                        receivedPackets.add(packet[0]);

                        if (packet.length < DATASIZE) {
                            stop = true;
                        }
                    }
                }

            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    stop = true;
                }
            }

        }
        // write to the output file
        Utils.setFileContents(fileContents, getFileID());
    }

    private boolean previousPacketsReceived(Integer[] packet, Set<Integer> receivedPackets) {
        for (int i = 1; i < packet[0]; i++) {
            if (!receivedPackets.contains(i)) {
                return false;
            }
        }
        return true;
    }

    private void sendAck(Integer[] packet) {
        Integer[] ackPacket = createEmptyPacket(packet[0]);
        getNetworkLayer().sendPacket(ackPacket);
        System.out.println("ACK sent for packet: " + packet[0]);
    }



}


