package misc.simulator;

import java.util.ArrayList;
import java.util.List;

import misc.bot.decisionMakers.DecisionMaker;
import misc.bot.decisionMakers.SimpleHeuristicDecisionMaker;
import misc.bot.moves.BotMove;
import misc.bot.moves.BuyDevCard;
import misc.bot.moves.PiecePlacement;
import misc.bot.moves.PlayDevCard;
import misc.bot.moves.PlayKnight;
import misc.bot.moves.PlayMonopoly;
import misc.bot.moves.PlayRoadBuilding;
import misc.bot.moves.PlayYOP;
import misc.bot.moves.Trade;
import misc.utils.ReducedGame;
import misc.utils.ReducedPlayer;
import misc.utils.exceptions.SimNotInitialisedException;
import soc.game.SOCDevCardConstants;
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
	int currentPlayerTurn = -1;

	/**
	 * Constructor. Given a player number and a SOCGame creates a simulator
	 * which can simulate the outcome of a game.
	 * 
	 * @param game
	 * @param ourPlayerNumber
	 */
	public Simulator(SOCGame game, int ourPlayerNumber) {
		this.game = game;
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
			dmArr[socPlayer.getPlayerNumber()] = dm;
		}
	}

	/**
	 * Pass in a game that is to be simulated. A copy is made of this game.
	 * 
	 * @param game
	 *            The reduced game being simulated.
	 */
	public void setReducedGame(ReducedGame game) {
		this.reducedGame = new ReducedGame(game);
	}

	/**
	 * Receive the turn number of the current player so we know when to start
	 * the simulation
	 * 
	 * @param turn
	 */
	public void setCurrentTurn(int turn) {
		this.currentPlayerTurn = turn;
	}

	/**
	 * Run the simulation of the game.
	 * 
	 * @return
	 * @throws SimNotInitialisedException
	 */
	public int runSimulator() throws SimNotInitialisedException {

		reducedGame.createDevCardDeck();

		if (currentPlayerTurn == -1) {
			throw new SimNotInitialisedException();
		}
		
		int turns =0;
		
		// Simulate until the end of the game
		while (!reducedGame.isGameFinished() && (turns < 100)) {
			// Roll and give resources to people
			int roll = DiceRoller.rollDice();
			if (roll == 7) {
				for (DecisionMaker dm : dmArr) {
					dm.setReducedGame(new ReducedGame(reducedGame));
					int[] discarded = dm.getRobberDiscard(0);
					reducedGame.handlePlayerDiscard(discarded, dm.getOurPlayerNumber());
					if(dm.getOurPlayerNumber() == currentPlayerTurn){
						int robberLocation = dm.getNewRobberLocation();
						reducedGame.handleMoveRobber(robberLocation, dm.getOurPlayerNumber(), dm.getRobberTarget());
					}
				}
			} else {
				reducedGame.assignResources(roll);
			}

			// Pass the player a copy of the reduced game.
			DecisionMaker currentDM = dmArr[currentPlayerTurn];
			currentDM.setReducedGame(new ReducedGame(reducedGame));	
			
			//Hanging here?
			List<BotMove> movesToPlay = currentDM.getMoveDecision();		
			reducedGame.applyMoveSet(movesToPlay, currentPlayerTurn);

			// Move the turn to the next player.
			if (currentPlayerTurn == 3) {
				currentPlayerTurn = 0;
			} else {
				currentPlayerTurn++;
			}
			turns++;
			//System.gc();
		}

		// Find the player that won.
		ReducedPlayer winningPlayer = null;
		for (ReducedPlayer player : reducedGame.getPlayers()) {
			if (player.getVictoryPoints() == reducedGame.WINNING_VP) {
				winningPlayer = player;
			}
		}
		
		if(winningPlayer == null){
			return reducedGame.getHighestVP();
		}
		
		System.out.println("Winner = " + winningPlayer.getPlayerNumber() + " with VP:" + winningPlayer.getVictoryPoints() + " in turns: " + turns);
		return winningPlayer.getPlayerNumber();

	}

}
