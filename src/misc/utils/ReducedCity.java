package misc.utils;

import soc.game.SOCPlayingPiece;

/**
 * Class representing the basic information about a city.
 * @author david
 *
 */
public class ReducedCity extends ReducedBoardPiece{

	public ReducedCity(int location, int owner){
		super(location,owner,SOCPlayingPiece.CITY);
	}
	
	/**
	 * Copy constructor
	 * @param orig
	 */
	public ReducedCity(ReducedBoardPiece orig){
		super(orig.getLocation(),orig.getOwner(),orig.getType());
	}
	
}
