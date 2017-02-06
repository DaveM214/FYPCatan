package misc.bot.buildPlanning;

import java.util.ArrayList;

import misc.bot.moves.BotMove;
import misc.bot.moves.BuyDevCard;
import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

/**
 * To find out all the possible building we will take a search tree approach.
 * This tree shouldn't go particularly deep. Each node will store the resource
 * available. The parent node and the transition (bot move) that got us to this
 * node.
 * 
 * @author david
 *
 */
public class BuildNode {

	private final int numOre;
	private final int numWood;
	private final int numClay;
	private final int numWheat;
	private final int numSheep;

	private SOCGame game;
	private SOCPlayer ourPlayer;
	
	private BuildNode parentNode;
	private ArrayList<BuildNode> children;
	
	//The Move that got us to this state. Null if it is the root of the tree;
	private final BotMove parentMove;
	 
	/**
	 * Constructor - create a node of the tree with the relevant information passed in
	 * @param ore Amount of ore
	 * @param wood Amount of wood
	 * @param clay Amount of clay
	 * @param wheat Amount of wheat
	 * @param sheep Amount of sheep
	 * @param parentMove The move that generated - it
	 * @param parentNode The state of the game before this node 
	 * @param ourPlayer ourPlayer
	 * @param game The SOC game this is a part of.
	 */
	public BuildNode(int ore, int wood, int clay, int wheat, int sheep, BotMove parentMove, BuildNode parentNode, SOCPlayer ourPlayer, SOCGame game){
		this.game = game;
		this.ourPlayer = ourPlayer;
		this.numOre = ore;
		this.numWood = wood;
		this.numClay = clay;
		this.numWheat = wheat;
		this.numSheep = sheep;
		this.parentMove = parentMove;
		this.parentNode = parentNode;
		generateChildNodes();
	}
	
	public void generateChildNodes(){
		
		if(numWood >= 1 && numClay >= 1){
			//Build roads
		}
		
		if(numWood >= 1 && numClay >= 1 && numWheat >= 1 && numSheep >=1){
			//Build settlements
		}
		
		if(numOre >= 3 && numWheat >= 2){
			//Build cities
		}
		
		if(numOre >=1 && numWheat >= 1 && numSheep >=1 && game.getNumDevCards() > 0){
			BotMove move = new BuyDevCard();
			SOCPlayer playerCopy = new SOCPlayer(ourPlayer);
			int cards = game.getNumDevCards();
			children.add(new BuildNode(numOre-1,numWood,numClay,numWheat-1,numSheep -1, move, this, playerCopy,game));
		}
		
	}

	public int getNumOre() {
		return numOre;
	}

	public int getNumWood() {
		return numWood;
	}

	public int getNumClay() {
		return numClay;
	}

	public int getNumWheat() {
		return numWheat;
	}

	public int getNumSheep() {
		return numSheep;
	}

	public SOCGame getGame() {
		return game;
	}

	public SOCPlayer getOurPlayer() {
		return ourPlayer;
	}

	public BuildNode getParentNode() {
		return parentNode;
	}

	public ArrayList<BuildNode> getChildren() {
		return children;
	}

}
