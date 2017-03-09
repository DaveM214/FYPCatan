package misc.bot.mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import misc.bot.buildPlanning.BuildNode;
import misc.bot.moves.BotMove;

public abstract class TreeNode {

	protected TreeNode parent;
	protected List<TreeNode> children;
	protected List<TreeNode> unexploredChildren;
	protected int nodeType;
	protected BuildNode buildNode;
	protected int totalSimulations;
	protected int[] wonSimulations = new int[4];
	protected int playerTurn;

	public static final int DECISION_NODE = 0;
	public static final int CHANCE_NODE = 1;

	public TreeNode(int type, TreeNode parent, BuildNode buildNode) {
		this.nodeType = type;
		this.parent = parent;
		this.buildNode = buildNode;
		this.children = new ArrayList<TreeNode>();
		this.unexploredChildren = new ArrayList<TreeNode>();
	}

	public TreeNode getParent() {
		return parent;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public int getNodeType() {
		return nodeType;
	}

	public void addChildren(int type, List<BuildNode> childNodes) {
		for (BuildNode node : childNodes) {
			if (type == DECISION_NODE) {
				children.add(new DecisionNode(this, node));
			} else {

			}
		}
	}

	public void addUnexploredChildren(int type, List<BuildNode> unexploredChild) {
		for (BuildNode node : unexploredChild) {
			if (type == DECISION_NODE) {
				unexploredChildren.add(new DecisionNode(this, node));
			} else {
				unexploredChildren.add(new ChanceNode(this, node));
			}
		}
	}

	public BuildNode getBuildNode() {
		return this.buildNode;
	}

	public int getTotalSimulations() {
		return totalSimulations;
	}

	public int getWonSimulations(int pNum) {
		return wonSimulations[pNum];
	}

	public void incrementWonSimulations(int pNum) {
		this.wonSimulations[pNum]++;
	}

	public void incrementTotalSimulations() {
		this.totalSimulations++;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public int getPlayerTurn() {
		return playerTurn;
	}
	
	public void setPlayerTurn(int turn){
		this.playerTurn = turn;
	}


	/**
	 * Method to pick an expansion for a tree. For this we will pick a random
	 * expansion from the unexplored child set. Add it to the regular child set
	 * and then remove and return from the unexplored child set.
	 * 
	 * @return
	 */
	public TreeNode pickExpansion() {
		Random rand =  new Random();
		int i = rand.nextInt(unexploredChildren.size());
		TreeNode expansion = unexploredChildren.get(i);
		children.add(expansion);
		return unexploredChildren.remove(i);
	}

	/**
	 * Method for backing up the results of the win throughout the tree.
	 */
	public void propagateResult(int winner) {
		TreeNode node = this;
		while(node!=null){
			incrementTotalSimulations();
			incrementWonSimulations(winner);
			node = node.getParent();
		}
	}
	
	public abstract TreeNode selectNextNode();

	public boolean isTerminal() {
		return buildNode.getReducedGame().isGameFinished();
	}

	

}
