package misc.bot.buildPlanning;

import java.util.ArrayList;
import java.util.List;

import misc.bot.moves.BotMove;
import misc.bot.moves.BuyDevCard;
import misc.bot.moves.PiecePlacement;
import misc.utils.ReducedBoard;
import misc.utils.ReducedGame;
import misc.utils.ReducedPlayer;
import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCPlayingPiece;
import soc.game.SOCResourceConstants;
import sun.org.mozilla.javascript.GeneratedClassLoader;

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

	private int numOre;
	private int numWood;
	private int numClay;
	private int numWheat;
	private int numSheep;
	private int ourPlayerNumber;
	private ReducedGame game;

	private SOCGame referenceGame;
	private SOCPlayer ourPlayer;

	private BuildNode parentNode;
	private ArrayList<BuildNode> children;

	// The Move that got us to this state. Null if it is the root of the tree;
	private final BotMove parentMove;

	/**
	 * Constructor - create a node of the tree with the relevant information
	 * passed in
	 * 
	 * @param ore
	 *            Amount of ore
	 * @param wood
	 *            Amount of wood
	 * @param clay
	 *            Amount of clay
	 * @param wheat
	 *            Amount of wheat
	 * @param sheep
	 *            Amount of sheep
	 * @param parentMove
	 *            The move that generated - it
	 * @param parentNode
	 *            The state of the game before this node
	 * @param ourPlayer
	 *            ourPlayer
	 * @param game
	 *            The SOC game this is a part of.
	 */
	public BuildNode(ReducedGame game, BotMove parentMove, BuildNode parentNode, SOCPlayer ourPlayer,
			SOCGame referenceGame) {
		this.game = game;
		this.referenceGame = referenceGame;
		this.ourPlayer = ourPlayer;
		this.ourPlayerNumber = ourPlayer.getPlayerNumber();
		populateResouceFields();
		this.parentMove = parentMove;
		this.parentNode = parentNode;
		generateChildNodes();
	}

	private void populateResouceFields() {
		ReducedPlayer us = game.getOurPlayer();
		int[] res = us.getResources();
		numClay = res[0];
		numOre = res[1];
		numSheep = res[2];
		numWheat = res[3];
		numWood = res[4];
	}

	public void generateChildNodes() {
		// Will we need to consider the number of pieces available?

		// If we have enough for a road find all the road building locations
		if (numWood >= 1 && numClay >= 1 && game.getOurPlayer().getRoadPieces() > 0) {
			ReducedBoard board = game.getBoard();
			List<Integer> locations = board.getLegalRoadLocations(ourPlayerNumber);
			for (Integer location : locations) {
				ReducedGame gameCopy = new ReducedGame(game);
				gameCopy.getBoard().addRoad(location, ourPlayerNumber);
				ReducedPlayer usInCopy = gameCopy.getOurPlayer();
				usInCopy.decrementResource(SOCResourceConstants.CLAY - 1);
				usInCopy.decrementResource(SOCResourceConstants.WOOD - 1);
				usInCopy.decrementRoadPieces();
				BotMove move = new PiecePlacement(location, SOCPlayingPiece.ROAD);
				BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
				children.add(child);
			}
		}

		// If we have enough for a settlement find all the locations that we can
		// build it
		if (numWood >= 1 && numClay >= 1 && numWheat >= 1 && numSheep >= 1
				&& game.getOurPlayer().getSettlementPieces() > 0) {
			// Find all the places that we can build a settlement
			ReducedBoard board = game.getBoard();
			List<Integer> locations = board.getLegalSettlementLocations(ourPlayerNumber);

			// Add all the possible locations as child moves
			for (Integer location : locations) {
				ReducedGame gameCopy = new ReducedGame(game);
				gameCopy.getBoard().addSettlement(location, ourPlayerNumber);
				ReducedPlayer usInCopy = gameCopy.getOurPlayer();
				usInCopy.decrementResource(SOCResourceConstants.CLAY - 1);
				usInCopy.decrementResource(SOCResourceConstants.WOOD - 1);
				usInCopy.decrementResource(SOCResourceConstants.SHEEP - 1);
				usInCopy.decrementResource(SOCResourceConstants.WHEAT - 1);
				usInCopy.decrementSettlementPieces();
				BotMove move = new PiecePlacement(location, SOCPlayingPiece.SETTLEMENT);
				BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
				children.add(child);
			}

		}

		if (numOre >= 3 && numWheat >= 2 && game.getOurPlayer().getCityPieces() > 0) {
			// BuildCities
			ReducedBoard board = game.getBoard();
			List<Integer> locations = board.getLegalCityLocations(ourPlayerNumber);

			for (Integer location : locations) {
				ReducedGame gameCopy = new ReducedGame(game);
				gameCopy.getBoard().addCity(location, ourPlayerNumber);
				ReducedPlayer usInCopy = gameCopy.getOurPlayer();
				
				for (int i = 0; i < 2; i++) {
					usInCopy.decrementResource(SOCResourceConstants.ORE - 1);
					usInCopy.decrementResource(SOCResourceConstants.WOOD - 1);
				}
				
				usInCopy.decrementResource(SOCResourceConstants.ORE - 1);
				usInCopy.decrementCityPieces();
				usInCopy.incrementSettlementPieces();
				BotMove move = new PiecePlacement(location, SOCPlayingPiece.CITY);
				BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
				children.add(child);
			}
		}

		// We are only allowed one dev card so it is allowed set it to possible
		// buy
		if (numOre >= 1 && numWheat >= 1 && numSheep >= 1 && game.getDevCardsLeft() > 0) {
			BotMove move = new BuyDevCard();
			ReducedGame gameCopy = new ReducedGame(game);

			ReducedPlayer usInCopy = gameCopy.getOurPlayer();
			usInCopy.decrementResource(SOCResourceConstants.ORE - 1);
			usInCopy.decrementResource(SOCResourceConstants.WHEAT - 1);
			usInCopy.decrementResource(SOCResourceConstants.SHEEP - 1);
			gameCopy.decrementDevCards();

			BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
			children.add(child);
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
		return referenceGame;
	}

	public ReducedGame getReducedGame() {
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
