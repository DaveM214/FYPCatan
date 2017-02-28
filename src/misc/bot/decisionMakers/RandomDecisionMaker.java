package misc.bot.decisionMakers;

import java.util.ArrayList;
import java.util.Random;

import misc.bot.moves.BotMove;
import misc.simulator.Simulator;
import misc.utils.exceptions.SimNotInitialisedException;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

public class RandomDecisionMaker extends DecisionMaker{

	public RandomDecisionMaker(SOCGame game) {
		super(game);
	}

	@Override
	public ArrayList<BotMove> getMoveDecision() {
		
	/////////////////SIM//////	
	Simulator sim = new Simulator(game, getOurPlayerNumber());
		

		try {
			for (int i = 0; i < 100; i++) {
				System.out.println("Sim :" + i);
				sim.setReducedGame(reducedGame);
				sim.setCurrentTurn(getOurPlayerNumber());
				int winner = sim.runSimulator();
				System.out.println("Simulated Winnder: " +  winner);
			}
		} catch (SimNotInitialisedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	///////////////////////////////END SIM////////////
		ArrayList<ArrayList<BotMove>> possMoves = getAllPossibleMoves();
		System.out.println("Possible Moves: " + possMoves.size());
		Random rand = new Random();
		return possMoves.get(rand.nextInt(possMoves.size()));
	}

	@Override
	public int getNewRobberLocation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getRobberDiscard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRobberTarget() {
		// TODO Auto-generated method stub
		return 0;
	}

}
