package misc.bot.decisionMakers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import misc.bot.moves.BotMove;
import misc.simulator.Simulator;
import misc.utils.ReducedBoardPiece;
import misc.utils.ReducedGame;
import misc.utils.exceptions.SimNotInitialisedException;
import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCPlayingPiece;

public class RandomDecisionMaker extends DecisionMaker {

	private int hexWeAreRobbing;

	public RandomDecisionMaker(SOCGame game) {
		super(game);
	}

	@Override
	public ArrayList<BotMove> getMoveDecision() {

		///////////////// SIM//////
		/*
		try {
			for (int i = 0; i < 10000; i++) {
				Simulator sim = new Simulator(game, getOurPlayerNumber());
				System.out.println("Sim :" + i);
				sim.setReducedGame(new ReducedGame(reducedGame));
				sim.setCurrentTurn(getOurPlayerNumber());
				int winner = sim.runSimulator();
				System.gc();
			}
		} catch (SimNotInitialisedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/////////////////////////////// END SIM////////////
	*/
		ArrayList<ArrayList<BotMove>> possMoves = getAllPossibleMoves();
		Random rand = new Random();
		return possMoves.get(rand.nextInt(possMoves.size()));
	}


	@Override
	public int getNewRobberLocation() {
		List<Integer> robberLocations = getPossibleRobberLocations();
		Map<Integer,Integer> robberLocationScores = scoreRobberLocations(robberLocations);
		
		int bestScore = -1;
		int bestLocation = -1;
		
		for (Integer location : robberLocationScores.keySet()) {
			int score = robberLocationScores.get(location);
			if(score > bestScore){
				bestScore = score;
				bestLocation = location;
			}
		}
		
		hexWeAreRobbing = bestLocation;
		return bestLocation;
	}

	private int getBestIndex(List<Integer> robberLocationScores) {
		int bestIndex = -1;
		int bestScore = Integer.MIN_VALUE;
		for (int i = 0; i < robberLocationScores.size(); i++) {
			if (robberLocationScores.get(i) > bestScore) {
				bestIndex = i;
				bestScore = robberLocationScores.get(i);
			}
		}
		return bestIndex;
	}

	/**
	 * We will score the robber locations based on how much resource they will
	 * deprive our opponents of for this simple decision maker.
	 * 
	 * @param robberLocations
	 * @return
	 */
	private Map<Integer,Integer> scoreRobberLocations(List<Integer> robberLocations) {
		Map<Integer,Integer> scoreMap = new HashMap<Integer,Integer>();
		for (Integer location : robberLocations) {
			int score = 0;
			List<ReducedBoardPiece> surrounding = reducedGame.getBoard().getSettlementsAroundHex(location);
			for (ReducedBoardPiece reducedBoardPiece : surrounding) {
				if(reducedBoardPiece.getType() == SOCPlayingPiece.CITY){
					score+=2;
				}else{
					score++;
				}
			}
			scoreMap.put(location, score);
		}
		return scoreMap;
	}
	
	
	
	@Override
	public int[] getRobberDiscard(int discard) {
		return getDiscardResources(discard);
	}

	private int[] getDiscardResources(int resourcesToDiscard) {
		int[] ourResources = new int[5];
		System.arraycopy(getOurResources(), 0, ourResources, 0, ourResources.length);
		int[] discardSet = new int[] { 0, 0, 0, 0, 0 };
		int resourcesDiscarded = 0;
		int index = 0;

		while (resourcesDiscarded < resourcesToDiscard) {
			if (ourResources[index] > 0) {
				discardSet[index]++;
				ourResources[index]--;
				resourcesDiscarded++;
			}
			
			if (index == 4) {
				index = 0;
			} else {
				index++;
			}
			
		}
		return discardSet;
	}

	@Override
	
	/**
	 * The player we are going to take the resource from.
	 */
	public int getRobberTarget() {
		List<ReducedBoardPiece> possTargets = reducedGame.getBoard().getSettlementsAroundHex(hexWeAreRobbing);

		// Steal from a random target
		Random rand = new Random();
		return possTargets.get(rand.nextInt(possTargets.size())).getOwner();
	}

}
