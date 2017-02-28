package misc.utils;

import soc.game.SOCPlayingPiece;

/**
 * Class representing a reduced road. TODO complete this.
 * @author david
 *
 */
public class ReducedRoad extends ReducedBoardPiece{

	public ReducedRoad(int location, int owner){
		super(location,owner,SOCPlayingPiece.ROAD);
	}
	
	/**
	 * Copy constructor
	 * @param orig
	 */
	public ReducedRoad(ReducedBoardPiece orig){
		super(orig.getLocation(),orig.getOwner(),orig.getType());
	}
	
}
