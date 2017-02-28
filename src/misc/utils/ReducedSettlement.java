package misc.utils;

import soc.game.SOCPlayingPiece;

/**
 * Class representing a reduced settlement.
 * 
 * @author david
 *
 */
public class ReducedSettlement extends ReducedBoardPiece{

	public ReducedSettlement(int location, int owner){
		super(location,owner,SOCPlayingPiece.SETTLEMENT);
	}
	
	/**
	 * Copy constructor
	 * @param orig
	 */
	public ReducedSettlement(ReducedBoardPiece orig){
		super(orig.getLocation(),orig.getOwner(),orig.getType());
	}

}
