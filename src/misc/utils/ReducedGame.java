package misc.utils;

import java.util.ArrayList;
import java.util.List;

import soc.game.SOCGame;
import soc.game.SOCPlayer;

/**
 * Class representing the basic information about a Game.
 * 
 * @author david
 *
 */
public class ReducedGame {

	private final int ourPlayerNumber;
	private ReducedBoard board;
	private int devCardsLeft;
	private List<ReducedPlayer> players;
	public final static int WINNING_VP = 10;

	/**
	 * Constructor. Creates a reduced game from an existing SOCGame;
	 * 
	 * @param ourPlayerNumber
	 * @param game
	 */
	public ReducedGame(int ourPlayerNumber, SOCGame game) {
	
		this.ourPlayerNumber = ourPlayerNumber;
		this.devCardsLeft = game.getNumDevCards();
		this.board = new ReducedBoard(game.getBoard());
	
		SOCPlayer[] socPlayers = game.getPlayers();
		players = new ArrayList<ReducedPlayer>();	
		for (SOCPlayer socPlayer : socPlayers) {
			ReducedPlayer reducedPlayer = new ReducedPlayer(socPlayer);
			players.add(reducedPlayer);
		}
		
	}

	/**
	 * Copy constructor, create a new ReducedGame as a copy of the one passed in
	 * the to the parameters.
	 * 
	 * @param orig
	 */
	public ReducedGame(ReducedGame orig) {
		this.ourPlayerNumber = orig.getOurPlayerNumber();
		this.devCardsLeft = orig.getDevCardsLeft();
		this.board = new ReducedBoard(orig.getBoard());

		players = new ArrayList<ReducedPlayer>();
		for (ReducedPlayer player : orig.getPlayers()) {
			ReducedPlayer newPlayer = new ReducedPlayer(player);
			players.add(newPlayer);
		}

	}

	/**
	 * Get our player number
	 * 
	 * @return Our player number
	 */
	public int getOurPlayerNumber() {
		return this.ourPlayerNumber;
	}

	/**
	 * Get the number of remaining dev cards that can be bought
	 * 
	 * @return
	 */
	public int getDevCardsLeft() {
		return devCardsLeft;
	}

	/**
	 * Set the number of remaining dev cards that can be bought
	 * 
	 * @param cardsLeft
	 */
	public void SetDevCardsLeft(int cardsLeft) {
		this.devCardsLeft = cardsLeft;
	}

	/**
	 * Get the simple board for the game.
	 * 
	 * @return The reduced game board
	 */
	public ReducedBoard getBoard() {
		return this.board;
	}

	/**
	 * Get the object representing our simplified player.
	 * 
	 * @return Our reduced player.
	 */
	public ReducedPlayer getOurPlayer() {
		return players.get(ourPlayerNumber);
	}

	public List<ReducedPlayer> getPlayers() {
		return players;
	}
	
	/**
	 * Decrement the number of dev cards left.
	 */
	public void decrementDevCards(){
		devCardsLeft--;
	}

	/**
	 * Returns -1 if the game is ongoing and the number of the winning player if
	 * the correct number of victory points have been reached.
	 * 
	 * @return
	 */
	public int isFinished() {
		int winner = -1;
		for (ReducedPlayer reducedPlayer : getPlayers()) {
			if (reducedPlayer.getVictoryPoints() >= WINNING_VP) {
				winner = reducedPlayer.getPlayerNumber();
			}
		}
		return winner;
	}
	
	/**
	 * Get the player sitting at a specific seat. 0-3
	 * @param player The play number of the player we want
	 * @return The reduced player object referred to by the number.
	 */
	public ReducedPlayer getPlayer(int player){
		return players.get(player);
	}

}
