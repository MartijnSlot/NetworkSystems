package Location;

import Utils.MacRssiPair;
import Utils.Position;
import Utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * BadAssLocationFinder
 * @author Martijn Slot
 */
public class BadAssMofoLocationFinder implements LocationFinder {

    private Map<String, Position> knownLocations = new HashMap<>(); //Contains the known locations of APs. The long is a MAC address.
    private AccessPoint accessPointA;
    private AccessPoint accessPointB;
    private AccessPoint accessPointC;

    public BadAssMofoLocationFinder() {
        knownLocations = Utils.getKnownLocations(); //Put the known locations in our hashMap
        initAccessPoints();
    }

    private void initAccessPoints() {
        String apAMac = "64:D9:89:43:C1:50";
        String apBMac = "64:D9:89:46:01:30";
        String apCMac = "64:D9:89:43:C4:B0";
        Position positionA = knownLocations.get(apAMac);
        int[] distancesA = {0, 78, 87};
        int[] rssisA = {-47,-67,-77};
        Position positionB = knownLocations.get(apBMac);
        int[] distancesB = {0,78,39};
        int[] rssisB = {-41,-66,-66};
        Position positionC = knownLocations.get(apCMac);
        int[] distancesC = {0,39,89};
        int[] rssisC = {-38,-68,-65};
        accessPointA = new AccessPoint(apAMac, positionA, distancesA, rssisA);
        accessPointB = new AccessPoint(apBMac, positionB, distancesB, rssisB);
        accessPointC = new AccessPoint(apCMac, positionC, distancesC, rssisC);
    }

    private Position calculatePosition(MacRssiPair[] data) {
        int rssiA = getAccesPointSignalStrenth(accessPointA, data);
        int rssiB = getAccesPointSignalStrenth(accessPointB, data);
        int rssiC = getAccesPointSignalStrenth(accessPointC, data);
        Set<Position> positions = getOverlappingPositions(accessPointA, accessPointB, accessPointC, rssiA, rssiB, rssiC);
        return calculateCenter(positions);
    }

    private Integer getAccesPointSignalStrenth(AccessPoint accessPoint, MacRssiPair[] data) {
        for (MacRssiPair macpair : data) {
            if (accessPoint.getMacAddress().equals(macpair.getMacAsString())) {
                return macpair.getRssi();
            }
        }
        return 0;
    }

    private Position calculateCenter(Set<Position> positions) {
        double minX = 0.0;
        double maxX = 0.0;
        double minY = 0.0;
        double maxY = 0.0;
        double posXsum = 0.0;
        double posYsum = 0.0;

        for(Position position : positions) {
            posXsum += position.getX();
            posYsum += position.getY();
        }

        double avgX = posXsum / (double)positions.size();
        double avgY = posYsum / (double)positions.size();
        return new Position(avgX, avgY);
    }

    private Set<Position> getOverlappingPositions(AccessPoint APa, AccessPoint APb, AccessPoint APc, int rssiA, int rssiB, int rssiC){
        Set<Position> squareA = APa.getDistanceSquare(rssiA);
        Set<Position> squareB = APb.getDistanceSquare(rssiB);
        Set<Position> squareC = APc.getDistanceSquare(rssiC);
//        System.out.println(squareA.toString());
        Set<Position> overlappingPositions = new HashSet<Position>();

        while (overlappingPositions.size() == 0) {
            for (Position posA : squareA) {
                for (Position posB : squareB) {
                    if (posA.equals(posB)) {
                        for (Position posC : squareC) {
                            if (posC.equals(posA)) {
                                overlappingPositions.add(posA);
                            }
                        }
                    }
                }
            }
            squareA = increaseSquares(squareA);
            squareB = increaseSquares(squareB);
            squareC = increaseSquares(squareC);
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
        System.out.println("minY = " + minY);
        System.out.println("maxY = " + maxY);
        System.out.println("minX = " + minX);
        System.out.println("maxX = " + maxX);

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

    @Override
    public Position locate(MacRssiPair[] data) {
        printMacs(data); //print all the received data
        return getLocation(data); //return the first known APs location
    }

    private Position getLocation(MacRssiPair[] data) {
        return calculatePosition(data);
    }

    /**
     * Returns the position of the first known AP found in the list of MacRssi pairs
     * @param data
     * @return
     */
    private Map<Position, Integer> getCloseKnownLocationsFromList(MacRssiPair[] data) {
        Map<Position, Integer> locationRssi = new HashMap<Position, Integer>();
        Map<Position, Integer> locationWeight = new HashMap<Position, Integer>();
        Integer lowestMapvalue;

        for (MacRssiPair macpair : data) {
            if (knownLocations.containsKey(macpair.getMacAsString())) {
                locationRssi.put(knownLocations.get(macpair.getMacAsString()), macpair.getRssi());
            }
        }

        lowestMapvalue = Collections.min(locationRssi.values());

        for (Position pos : locationRssi.keySet()) {
            locationWeight.put(pos, lowestMapvalue / locationRssi.get(pos));
            System.out.println("Position: " + pos.toString() +
                    " factor: " + lowestMapvalue / locationRssi.get(pos));
        }

        System.out.println("Number of known locations in the data: " + locationRssi.size());

        return locationWeight;
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
}