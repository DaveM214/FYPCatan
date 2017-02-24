package misc.simulator;

import misc.bot.DecisionMaker;
import misc.bot.SimpleHeuristicDecisionMaker;
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
	SOCGame game;
	int ourPlayerNumber;
	SOCPlayer[] players;
	DecisionMaker[] dmArr;

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
		dmArr = new DecisionMaker[players.length];
		
		for (SOCPlayer socPlayer : players) {
			DecisionMaker dm = new SimpleHeuristicDecisionMaker(game);
			dm.setOurPlayerInformation(socPlayer);
		}
		
	}

}
