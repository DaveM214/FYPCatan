package misc.utils;

import java.util.List;

import soc.game.SOCBoard;

/**
 * More workable class for search and simulation that uses a cut down version of
 * the board that is faster and easier to work with in terms of making copies of
 * various items. In this particular representation we won't care about the
 * hexes and their resources as this can always be referred back to the original
 * board
 * 
 * @author david
 *
 */
public class ReducedBoard {

	private List<ReducedRoad> roads;
	private List<ReducedSettlement> settlements;
	private List<ReducedCity> city;
	
	
	/**
	 * Constructor that creates a reduced board from a regular SOCBoard.
	 * 
	 * @param board
	 */
	public ReducedBoard(SOCBoard board) {

	}

	/**
	 * Copy constructor. Makes a copy of a reduced board passed in as a
	 * parameter.
	 * 
	 * @param orig
	 */
	public ReducedBoard(ReducedBoard orig) {

	}

}
