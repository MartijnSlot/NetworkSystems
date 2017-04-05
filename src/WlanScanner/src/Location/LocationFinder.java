package Location;
import Utils.MacRssiPair;
import Utils.Position;

/**
 * Interface for your LocationFinder
 * @author Bernd
 *
 */
public interface LocationFinder {
	
	public Position locate(MacRssiPair[] data);
	
}
