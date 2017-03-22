package misc.bot.decisionMakers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import misc.bot.buildPlanning.BuildNode;
import misc.bot.mcts.DecisionNode;
import misc.bot.mcts.TreeNode;
import misc.bot.moves.BotMove;
import misc.simulator.Simulator;
import misc.utils.ReducedBoardPiece;
import misc.utils.ReducedGame;
import misc.utils.exceptions.SimNotInitialisedException;
import soc.game.SOCGame;
import soc.game.SOCPlayingPiece;

public class MonteCarloDecisionMaker extends DecisionMaker {

	private int simulationCount = 0;
	public static final int MAXIMUM_SIMULATIONS = 10000;
	private int hexWeAreRobbing = 0;
	
	private final static int SETTLEMENT_WINS = 28;
	private final static int DEV_CARD_WINS = 5;
	private final static int CITY_WINS = 40;

	// This is the theortical best value of C - will need to be changed.
	// Directs how much exploring we do -
	public static final double EXPLORATION_PARAM = 1.1;

	public MonteCarloDecisionMaker(SOCGame game) {
		super(game);

	}

	@Override
	/**
	 * Main method for carrying out the tree search.
	 * 
	 */
	public ArrayList<BotMove> getMoveDecision() {
		simulationCount = 0;

		// Initialise the root node of the tree.
		BuildNode rootBuildNode = new BuildNode(reducedGame, null, null, ourPlayer, game);
		TreeNode root = new DecisionNode(null, rootBuildNode);
		root.setPlayerTurn(reducedGame.getOurPlayerNumber());
		root.addUnexploredChildren(TreeNode.CHANCE_NODE, getPossibleChildStates(rootBuildNode));

		// START THE TREE SEARCH
		while (simulationCount < MAXIMUM_SIMULATIONS) {

			// 1 - Selection - we select the next node.
			TreeNode nextNode = root.selectNextNode();

			// 2 - Expand that node get all of its children (or just one?)

			// If the node is terminal it can't be expanded - no point running
			// sim.
			if (nextNode.isTerminal()) {
				handleTerminalState(nextNode);
			} else {

				// This expansion node will be a chance node. Signifies the
				// moves being played.
				TreeNode expansion = nextNode.pickExpansion();

				// 3 - Do a simulation
				Simulator sim = new Simulator(game, getOurPlayerNumber());
				sim.setReducedGame(new ReducedGame(expansion.getBuildNode().getReducedGame()));
				sim.setCurrentTurn(expansion.getPlayerTurn());
				int winner = -1;
				try {
					winner = sim.runSimulator();
				} catch (SimNotInitialisedException e) {

				}
				simulationCount++;
				if (simulationCount % 50 == 0) {
					System.gc();
				}
				System.out.println("Simulations run: " + simulationCount);

				// 4 - Back the results up the tree.
				expansion.propagateResult(winner);
			}
		}

		/// We have done all of the simulations that we are allowed to do - find
		/// the child move that had the most simulations played.

		int mostSims = Integer.MIN_VALUE;
		TreeNode bestMove = null;

		for (TreeNode possibleMove : root.getChildren()) {
			if (possibleMove.getTotalSimulations() > mostSims) {
				bestMove = possibleMove;
				mostSims = possibleMove.getTotalSimulations();
			}
		}

		return generateMovesFromNode(bestMove.getBuildNode());
	}

	private void handleTerminalState(TreeNode nextNode) {
		int winner = nextNode.getBuildNode().getReducedGame().isFinished();
		nextNode.incrementTotalSimulations();
		nextNode.incrementWonSimulations(winner);
		nextNode.propagateResult(winner);
	}
	
	//////GENERIC METHODS


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
