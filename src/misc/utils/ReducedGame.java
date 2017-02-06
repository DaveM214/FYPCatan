package misc.utils;

import java.util.List;

import soc.game.SOCGame;

/**
 * Class representing the basic information about a Game.
 * @author david
 *
 */
public class ReducedGame {

	private final int ourPlayerNumber;
	private ReducedBoard board;
	private int devCardsLeft;
	private List<ReducedPlayer> players;
	
	/**
	 * Constructor. Creates a reduced game 
	 * 
	 * @param ourPlayerNumber
	 * @param game
	 */
	public ReducedGame(int ourPlayerNumber, SOCGame game){
		this.ourPlayerNumber = ourPlayerNumber;
		this.devCardsLeft = game.getNumDevCards();
		this.board = new ReducedBoard(game.getBoard());
		
	}
	
	/**
	 * Copy constructor, create a new ReducedGame as a copy of the one passed in the to the parameters.
	 * @param orig
	 */
	public ReducedGame(ReducedGame orig){
		this.ourPlayerNumber = orig.getOurPlayerNumber();
		this.board = new ReducedBoard(orig.getBoard());
		
	}
	
	/**
	 * Get our player number
	 * 
	 * @return Our player number
	 */
	public int getOurPlayerNumber(){
		return this.ourPlayerNumber;
	}
	
	/**
	 * Get the number of remaining dev cards that can be bought
	 * @return
	 */
	public int getDevCardsLeft(){
		return this.getDevCardsLeft();
	}
	
	/**
	 * Set the number of remaining dev cards that can be bought
	 * @param cardsLeft
	 */
	public void SetDevCardsLeft(int cardsLeft){
		this.devCardsLeft = cardsLeft;
	}
	
	/**
	 * Get the simple board for the game.
	 * @return The reduced game board
	 */
	public ReducedBoard getBoard(){
		return this.board;
	}
	
	/**
	 * Get the object representing our simplified player.
	 * @return Our reduced player.
	 */
	public ReducedPlayer getOurPlayer(){
		return players.get(ourPlayerNumber);
	}
	
	
	
	
	
}
