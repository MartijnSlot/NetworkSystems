package Location;

import Utils.Position;
import java.util.HashSet;
import java.util.Set;

/**
 * AccessPoint class defining a router
 * @author Mark Banierink
 */
public class AccessPoint {

    private String macAddress;
    private Position position;
    private double attenuation;
    private int[] distances;
    private int[] rssis;

    public AccessPoint(String mac, Position position, int[] distances, int[] rssis) {
        this.macAddress = mac;
        this.position = position;
        this.distances = distances;
        this.rssis = rssis;
        this.attenuation = calculateAttenuation();
    }

    public String getMacAddress() {
        return macAddress;
    }

    private int getDistance(int i) {
        return distances[i];
    }

    private double getRssiDifference(int i) {
        return rssis[i] - getBaseRssi();
    }

    private double getBaseRssi() {
        return rssis[0];
    }

    private int getNumberRouters() {
        return distances.length;
    }

    private double calculateAttenuation() {
        double attenuation1 = getRssiDifference(1) / (double)getDistance(1);
        double attenuation2 = getRssiDifference(2) / (double)getDistance(2);
        return (attenuation1+attenuation2) / 2.0;
    }

    public Position getPosition() {
        return position;
    }

    public int distanceToRouter(int rssi) {
        return (int)((rssi - getBaseRssi()) / attenuation);
    }

    public Set<Position> getDistanceSquare(int rssi) {
        Set<Position> positionSet = new HashSet<>();
        int xStart = (int)position.getX() - distanceToRouter(rssi);
        int xEnd = (int)position.getX() + distanceToRouter(rssi);
        int yStart = (int)position.getY() - distanceToRouter(rssi);
        int yEnd = (int)position.getY() + distanceToRouter(rssi);

        for (int x = xStart; x < xEnd; x++) {
            for (int y = yStart; y < yEnd; y++) {
                positionSet.add(new Position(x, y));
            }
        }
        return positionSet;
    }
}