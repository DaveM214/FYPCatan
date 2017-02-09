package misc.utils;

/**
 * Class representing the basic information about a city.
 * @author david
 *
 */
public class ReducedCity {

	private int location;
	private int owner;

	public ReducedCity(int location, int owner) {
		this.location = location;
		this.owner = owner;
	}

	public ReducedCity(ReducedCity orig){
		this.location = orig.getLocation();
		this.owner = orig.getOwner();
	}

	private int getOwner() {
		return owner;
	}

	public int getLocation() {
		return location;
	}
	
}
