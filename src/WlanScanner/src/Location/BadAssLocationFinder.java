package Location;

import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by martijn.slot on 05/04/2017.
 */
public class BadAssLocationFinder implements LocationFinder {

    private Map<String, Position> knownLocations = new HashMap<>(); //Contains the known locations of APs. The long is a MAC address.
    private AccessPoint accessPointA;
    private AccessPoint accessPointB;
    private AccessPoint accessPointC;

    public BadAssLocationFinder(){
        knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
    }

    @Override
    public Position locate(MacRssiPair[] data) {
        printMacs(data); //print all the received data
        return getLocation(data); //return the first known APs location
    }



    private Position getLocation(MacRssiPair[] data) {
        Position myPos = new Position (0,0);

        Set<Position> closeKnownLocationsFromList = getCloseKnownLocationsFromList(data);

        return myPos;
    }

    /**
     * Returns the position of the first known AP found in the list of MacRssi pairs
     * @param data
     * @return
     */
    private Set<Position> getCloseKnownLocationsFromList(MacRssiPair[] data){
        Set<Position> locationRssi = new HashSet<Position>();


        System.out.println("Number of close known locations: " + locationRssi.size());

        return locationRssi;
    }

    private void initAccessPoints() {
        String apAMac = "64:D9:89:43:C1:50";
        String apBMac = "64:D9:89:46:01:30";
        String apCMac = "64:D9:89:43:C4:B0";
        Position positionA = knownLocations.get(apAMac);
        int[] distancesA = {0, 78, 87};
        int[] rssisA = {-47,-67,-77};
        Position positionB = knownLocations.get(apBMac);
        int[] distancesB = {78,0,39};
        int[] rssisB = {-66,-4,-66};
        Position positionC = knownLocations.get(apCMac);
        int[] distancesC = {87,39,0};
        int[] rssisC = {-68,-65,-38};
        accessPointA = new AccessPoint(apAMac, positionA, distancesA, rssisA);
        accessPointB = new AccessPoint(apBMac, positionB, distancesB, rssisB);
        accessPointC = new AccessPoint(apCMac, positionC, distancesC, rssisC);
    }

    private Integer getAccesPointSignalStrenth(AccessPoint accessPoint, MacRssiPair[] data) {

        for (MacRssiPair macpair : data) {
            if (accessPoint.getMacAddress().equals(macpair.getMacAsString())) {
                return macpair.getRssi();
            }
        }
        return 0;
    }


    /**
     * Outputs all the received MAC RSSI pairs to the standard out
     * This method is provided so you can see the data you are getting
     * @param data
     */
    private void printMacs(MacRssiPair[] data) {
        for (MacRssiPair pair : data) {
            System.out.println(pair);
        }
    }

    private Set<Position> getOverlappingPositions(AccessPoint APa, AccessPoint APb, AccessPoint APc, int rssi){
        Set<Position> squareA = APa.getDistanceSquare(rssi);
        Set<Position> squareB = APb.getDistanceSquare(rssi);
        Set<Position> squareC = APc.getDistanceSquare(rssi);
        Set<Position> overlappingPositions = new HashSet<Position>();

        while (overlappingPositions.size() == 0) {
            for (Position posA : squareA) {
                for (Position posB : squareB) {
                    for (Position posC : squareC) {
                        if (posA == posB && posA == posC) {
                            overlappingPositions.add(posA);
                        }
                    }
                }
            }
            increaseSquares(squareA);
        }
        return overlappingPositions;
    }

    private Set<Position> increaseSquares(Set<Position> square) {
        double maxX = 0.0;
        double minX = 1000.0;
        double maxY = 0.0;
        double minY = 1000.0;

        for (Position pos : square) {
            if(pos.getX() > maxX) {
                maxX = pos.getX();
            } else if (pos.getX() < minX) {
                minX = pos.getX();
            }

            if (pos.getY() > maxY) {
                maxY = pos.getY();
            } else if (pos.getY() < minY) {
                minY = pos.getY();
            }
        }
        maxX = maxX + 1;
        minX = minX - 1;
        maxY = maxY + 1;
        minY = minY - 1;

        for (int i = (int)minY; i <= maxY; i++) {
            square.add(new Position(maxX, i));
        }

        for (int i = (int)minY; i <= maxY; i++) {
            square.add(new Position(minX, i));
        }

        for (int i = (int)minX; i <= maxX; i++) {
            square.add(new Position(i, minY));
        }

        for (int i = (int)minX; i <= maxX; i++) {
            square.add(new Position(i, maxY));
        }

        return square;
    }

}
