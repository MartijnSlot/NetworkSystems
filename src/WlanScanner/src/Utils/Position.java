package Utils;
/**
 * Represents an position with x and y coordinates
 * The timestamp gets automatically set during creation of the object
 * @author Bernd
 *
 */
public class Position {
	
	private double x;
	private double y;
	private long timestamp;
	
	public Position(double x, double y){
		this.x = x;
		this.y = y;
		timestamp = System.currentTimeMillis();
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	@Override
	public String toString(){
		return "("+x+","+y+")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Position position = (Position) o;

		if (Double.compare(position.x, x) != 0) return false;
		return Double.compare(position.y, y) == 0;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
