package misc.utils;

/**
 * Class representing a reduced settlement.
 * 
 * @author david
 *
 */
public class ReducedSettlement extends ReducedBoardPiece{

	public ReducedSettlement(int location, int owner){
		super(location,owner);
	}
	
	/**
	 * Copy constructor
	 * @param orig
	 */
	public ReducedSettlement(ReducedBoardPiece orig){
		super(orig.getLocation(),orig.getOwner());
	}

}
