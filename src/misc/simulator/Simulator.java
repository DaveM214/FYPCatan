package misc.simulator;

import misc.utils.ReducedGame;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

/**
 * Simulator that given a game state will simulate the game through to its
 * completion and return the winning player.
 * 
 * @author david
 *
 */
public class Simulator {

	ReducedGame reducedGame;
	int ourPlayerNumber;
	SOCPlayer[] players;

	/**
	 * Constructor. Given a player number and a SOCGame creates a simulator
	 * which can simulate the outcome of a game.
	 * 
	 * @param game
	 * @param ourPlayerNumber
	 */
	public Simulator(SOCGame game, int ourPlayerNumber) {
		this.reducedGame = new ReducedGame(ourPlayerNumber, game);
		this.ourPlayerNumber = ourPlayerNumber;
		players = game.getPlayers();
		initialiseDecisionMakers();
	}

	private void initialiseDecisionMakers() {
		// TODO Auto-generated method stub

	}

}
