package misc.bot;

import java.util.ArrayList;

import misc.bot.moves.BotMove;
import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

public abstract class DecisionMaker {

	private SOCGame game;
	private SOCPlayer ourPlayer;

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

	public void setOurPlayerInformation(SOCPlayer ourPlauer) {
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

	public abstract ArrayList<BotMove> getMoveDecision();

}
