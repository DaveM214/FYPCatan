package misc.utils;

/**
 * Class representing a reduced road. TODO complete this.
 * @author david
 *
 */
public class ReducedRoad {

	private final int location;
	private final int owner;
	
	public ReducedRoad(int location, int owner){
		this.location = location;
		this.owner = owner ;
	}
	
	/**
	 * Copy constructor
	 * @param orig
	 */
	public ReducedRoad(ReducedRoad orig){
		this.location = orig.getLocation();
		this.owner = orig.getOwner();
	}
	
	public int getLocation(){
		return location;
	}
	
	public int getOwner(){
		return owner;
	}
	
}
