package misc.bot.decisionMakers;

import java.util.ArrayList;
import java.util.List;

import misc.bot.buildPlanning.BuildNode;
import misc.bot.mcts.DecisionNode;
import misc.bot.mcts.TreeNode;
import misc.bot.moves.BotMove;
import misc.simulator.Simulator;
import misc.utils.ReducedGame;
import misc.utils.exceptions.SimNotInitialisedException;
import soc.game.SOCGame;

public class MonteCarloDecisionMaker extends DecisionMaker {

	private int simulationCount = 0;
	public static final int MAXIMUM_SIMULATIONS = 5000;

	// This is the theortical best value of C - will need to be changed.
	// Directs how much exploring we do - 
	public static final double EXPLORATION_PARAM = Math.sqrt(2);

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
				if(simulationCount%100 == 0){
					System.gc();
				}
				System.out.println("Simulations run: " + simulationCount );

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
