package misc.utils;

/**
 * Class representing a reduced settlement.
 * 
 * @author david
 *
 */
public class ReducedSettlement {

	private int location;
	private int owner;

	public ReducedSettlement(int location, int owner) {
		this.location = location;
		this.owner = owner;
	}

	public ReducedSettlement(ReducedSettlement orig){
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
