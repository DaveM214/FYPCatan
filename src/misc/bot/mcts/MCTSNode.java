package misc.bot.mcts;

import java.util.ArrayList;

import soc.game.SOCGame;
import soc.game.SOCPlayer;

/** 
 * Class representing the node of an MCTS Search tree.
 * @author david
 *
 */
public class MCTSNode {

	private MCTSNode parent;
	private ArrayList<MCTSNode> children;
	private SOCPlayer ourPlayer;
	private SOCGame game;
	private boolean terminalState;
	
	public MCTSNode(MCTSNode parent, SOCGame game, SOCPlayer ourPlayer){
		
	}
	
	/**
	 * Generate the child nodes
	 */
	public void generateChildren(){
		
	}
	
}
