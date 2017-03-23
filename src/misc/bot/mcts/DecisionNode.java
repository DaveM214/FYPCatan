package misc.bot.mcts;

import java.util.ArrayList;
import java.util.List;

import misc.bot.buildPlanning.BuildNode;
import misc.bot.decisionMakers.MonteCarloDecisionMaker;
import misc.bot.moves.BotMove;

public class DecisionNode extends TreeNode {

	/**
	 * Node for the MCTS tree
	 * 
	 * @param parent
	 *            The parent of this node - null if root
	 * @param node
	 *            The build node this node contains.
	 */
	public DecisionNode(TreeNode parent, BuildNode node) {
		super(TreeNode.DECISION_NODE, parent, node);
		if (parent != null) {
			this.playerTurn = parent.getPlayerTurn();
		}
	}

	@Override
	public TreeNode selectNextNode() {
		// If this is a leaf node then we have reached the node we want to find.
		if (unexploredChildren()) {
			return this;
		} else {

			// Score each node in the child set.
			TreeNode root = this;
			while (root.parent != null) {
				root = root.parent;
			}
			
			int allSimulations = root.getTotalSimulations();
			
			int bestIndex = 0;
			double bestScore = Double.MIN_VALUE;
			for (int i = 0; i < children.size(); i++) {
				double score = scoreChild(children.get(i), allSimulations);
				if (score > bestScore) {
					bestScore = score;
					bestIndex = i;
				}
			}

			return children.get(bestIndex).selectNextNode();
		}

	}

	private boolean unexploredChildren() {
		return unexploredChildren.size() > 0;
	}

	/**
	 * Helper function to calculate UCT for a node.
	 * 
	 * @param child
	 *            The child we are working out the score of.
	 * @param simulationsRun
	 *            The total number of sims so far in the whole simulation
	 * @return The UCT value.
	 */
	private double scoreChild(TreeNode child, int simulationsRun) {
		return (child.getAdjustedWonSimulations(playerTurn) / child.getAdjustedTotalSimulations())
				+ MonteCarloDecisionMaker.EXPLORATION_PARAM
						* Math.sqrt(Math.log(simulationsRun) / child.getTotalSimulations());
	}

}
