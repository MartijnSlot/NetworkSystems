package Utils;

import java.util.HashMap;

public class Utils {

	/**
	 * Make a long from a byte[6], useful for storing the MAC addresses
	 * 
	 * @param bytes
	 * @return
	 */
	public static long macToLong(byte[] bytes) {
		long ret = 0;
		ret += (bytes[0] & 0xFF) << 40;
		ret += (bytes[1] & 0xFF) << 32;
		ret += (bytes[2] & 0xFF) << 24;
		ret += (bytes[3] & 0xFF) << 16;
		ret += (bytes[4] & 0xFF) << 8;
		ret += (bytes[5]) & 0xFF;
		return ret;
	}

	/**
	 * Returns a HashMap of the known AP locations as a <String, Postion> The string
	 * is used because this is easily searchable for the hashMap
	 * 
	 * @return
	 */
	public static HashMap<String, Position> getKnownLocations() {

		HashMap<String, Position> knownLocations = new HashMap<String, Position>();

		// APs Oost Horst 115
		knownLocations.put("64:D9:89:43:C7:D0", new Position(112,45));	//ap3600-0102  THIS ONE IS NOT BROADCASTING!
		knownLocations.put("64:D9:89:43:C1:50", new Position(190,45));	//ap3600-0099
		knownLocations.put("64:D9:89:46:01:30", new Position(190,6));	//ap3600-0100		
		knownLocations.put("64:D9:89:43:C4:B0", new Position(112,6));	//ap3600-0101
		
		// APs Oost Horst 116
		knownLocations.put("64:D9:89:43:CF:E0", new Position(58,6));	//ap3600-0104
		knownLocations.put("64:D9:89:43:D4:F0", new Position(10,6));	//ap3600-0105
		knownLocations.put("64:D9:89:43:CD:60", new Position(10,45));	//ap3600-0106
		knownLocations.put("64:D9:89:43:D0:00", new Position(58,45));	//ap3600-0103  THIS ONE IS NOT BROADCASTING!		

		return knownLocations;
	}

}

