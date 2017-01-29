package misc.bot;

import java.util.ArrayList;

import misc.bot.moves.BotMove;
import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

/**
 * This is the class than when provided with the current board available in an
 * {@link SOCGame} will formulate a series of moves than will be played. This
 * class is abstract and provides utilities available for a decision making AI
 * class that wishes to search or otherwise work out what move to play. How this
 * information is utilised is left to the individual classes that extend this
 * one.
 * 
 * @author david
 *
 */
public abstract class DecisionMaker {

	private SOCGame game;
	private SOCPlayer ourPlayer;

	/**
	 * Constructor
	 * 
	 * @param game
	 * @param ourPlayer
	 */
	public DecisionMaker(SOCGame game, SOCPlayer ourPlayer) {
		this.game = game;
		this.ourPlayer = ourPlayer;
	}

	/**
	 * Update the game state that we are making our decision based on.
	 */
	public void updateGame(SOCGame updatedGame) {
		game = updatedGame;
	}

	/**
	 * Method to indicate which player in the game is us and set the field which
	 * stores this information
	 * 
	 * @param ourPlayer
	 *            Our player information
	 */
	public void setOurPlayerInformation(SOCPlayer ourPlayer) {
		this.ourPlayer = ourPlayer;
	}

	/**
	 * Method works out what moves are possible and returns a list of all the
	 * lists of moves.
	 */
	public ArrayList<ArrayList<BotMove>> getAllPossibleMoves() {
		ArrayList<ArrayList<BotMove>> possibles = new ArrayList<ArrayList<BotMove>>();

		return possibles;
	}

	/**
	 * Abstract method that must be implemented. Return a list of
	 * {@link BotMove} that should be executed by the {@link BotBrain}.
	 * 
	 * @return A list of moves that should be implemented in the coming turn.
	 */
	public abstract ArrayList<BotMove> getMoveDecision();

}
