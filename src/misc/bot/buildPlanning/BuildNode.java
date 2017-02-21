package misc.bot.buildPlanning;

import java.util.ArrayList;
import java.util.List;

import misc.bot.moves.BotMove;
import misc.bot.moves.BuyDevCard;
import misc.bot.moves.PiecePlacement;
import misc.bot.moves.Trade;
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

	private int ourPlayerNumber;
	private ReducedGame game;

	private SOCGame referenceGame;
	private SOCPlayer ourPlayer;

	private BuildNode parentNode;
	private List<BuildNode> children;
	private boolean[] bankRecResources;
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
		this.children = new ArrayList<BuildNode>();
		this.game = game;
		this.referenceGame = referenceGame;
		this.ourPlayer = ourPlayer;
		this.ourPlayerNumber = ourPlayer.getPlayerNumber();
		// populateResouceFields();
		this.parentMove = parentMove;
		this.parentNode = parentNode;
		generateChildNodes();

	}

	public void generateChildNodes() {
		// Will we need to consider the number of pieces available?
		ReducedPlayer us = game.getPlayer(ourPlayerNumber);
		int[] resources = us.getResources();

		// Bank trades
		handleBankTradeChildren(ourPlayerNumber);

		// If we have enough for a road find all the road building locations
		if (resources[SOCResourceConstants.WOOD - 1] >= 1 && resources[SOCResourceConstants.CLAY - 1] >= 1
				&& game.getOurPlayer().getRoadPieces() > 0) {
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
		if (resources[SOCResourceConstants.WOOD - 1] >= 1 && resources[SOCResourceConstants.CLAY - 1] >= 1
				&& resources[SOCResourceConstants.WHEAT - 1] >= 1 && resources[SOCResourceConstants.SHEEP - 1] >= 1
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

		if (resources[SOCResourceConstants.ORE - 1] >= 3 && resources[SOCResourceConstants.WHEAT - 1] >= 2
				&& game.getOurPlayer().getCityPieces() > 0) {
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
		if (resources[SOCResourceConstants.ORE - 1] >= 1 && resources[SOCResourceConstants.WHEAT - 1] >= 1
				&& resources[SOCResourceConstants.SHEEP - 1] >= 1 && game.getDevCardsLeft() > 0) {
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

	/**
	 * Handle populating all possible bank trades deals. We need to take into
	 * account the regular bank trade rate as well as possible reduced trade
	 * rate if we have a port and also specific resource ports.
	 * 
	 * TODO modify this method so that we can not try and receive a resource
	 * that we have used in the past.
	 */
	private void handleBankTradeChildren(int player) {
		int baseTradeRate = 4;

		// Check if we have 3:1 port
		if (game.getPlayer(player).hasGeneralPort()) {
			baseTradeRate--;
		}

		int[] tradeRates = new int[5];

		// Initialise an array of the trade rates.
		for (int i = 0; i < tradeRates.length; i++) {
			tradeRates[i] = baseTradeRate;
		}

		// See if there are any speciality ports
		boolean[] specPorts = game.getPlayer(player).hasSpecPorts();
		for (int i = 0; i < specPorts.length; i++) {
			if (specPorts[i]) {
				tradeRates[i] = 2;
			}
		}

		// Get the resources the player has
		int[] resources = game.getPlayer(player).getResources();

		for (int i = 0; i < resources.length; i++) {
			// We can trade this the resource to the bank
			// i = what we are trading --Check we haven't received this from the bank before
			// j = what we are receiving
			if (resources[i] >= tradeRates[i] && (game.getPlayer(player).getTradedResourceArray()[i] == false)) {
				for (int j = 0; j < resources.length; j++) {
					// We don't want to trade for itself
					if (j != i) {
						ReducedGame gameCopy = new ReducedGame(game);
						ReducedPlayer copyPlayer = gameCopy.getPlayer(player);

						// Set that we have received this resource and therefore
						// don't want to give it away
						copyPlayer.setTradedResourceArray(j, true);
						copyPlayer.incrementResource(j);
						for (int k = 0; k < tradeRates[i]; k++) {
							copyPlayer.decrementResource(i);
						}
						BotMove move = new Trade(i + 1, tradeRates[i], j + 1, 1, -1);
						BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
						children.add(child);
					}
				}
			}
		}

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

	public List<BuildNode> getChildren() {
		return children;
	}

	/**
	 * Get the move that resulted in this state.
	 * 
	 * @return
	 */
	public BotMove getParentMove() {
		return parentMove;
	}

}
