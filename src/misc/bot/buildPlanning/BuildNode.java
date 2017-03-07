package misc.bot.buildPlanning;

import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xerces.internal.dom.ChildNode;

import misc.bot.moves.BotMove;
import misc.bot.moves.BuyDevCard;
import misc.bot.moves.PiecePlacement;
import misc.bot.moves.PlayMonopoly;
import misc.bot.moves.PlayRoadBuilding;
import misc.bot.moves.PlayYOP;
import misc.bot.moves.Trade;
import misc.utils.GameUtils;
import misc.utils.ReducedBoard;
import misc.utils.ReducedGame;
import misc.utils.ReducedPlayer;
import soc.game.SOCBoard;
import soc.game.SOCDevCardConstants;
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

	private final SOCGame referenceGame;
	private SOCPlayer ourPlayer;

	private BuildNode parentNode;
	private List<BuildNode> children;
	private boolean[] bankRecResources;
	// The Move that got us to this state. Null if it is the root of the tree;
	private final BotMove parentMove;
	private int depth = 0;

	private boolean devCardsGenerated;
	private boolean tradesDone;
	private boolean buildingDone;

	/**
	 * 
	 * @param game The reduced game this build node is using.
	 * @param parentMove The move that caused this build node
	 * @param parentNode The node that was before this one.
	 * @param ourPlayer SOCPlayer representing us
	 * @param referenceGame SOCGame for reference/.
	 */
	public BuildNode(ReducedGame game, BotMove parentMove, BuildNode parentNode, SOCPlayer ourPlayer,
			SOCGame referenceGame) {
		int[] resources = game.getOurPlayer().getResources();
		this.children = new ArrayList<BuildNode>();
		this.game = game;
		this.referenceGame = referenceGame;
		this.ourPlayer = ourPlayer;
		this.ourPlayerNumber = ourPlayer.getPlayerNumber();
		// populateResouceFields();
		this.parentMove = parentMove;
		this.parentNode = parentNode;
		if (parentNode != null) {
			depth = this.parentNode.getDepth();
			depth++;
		}

		if (parentNode != null) {
			devCardsGenerated = parentNode.isDevCardsGenerated();
			tradesDone = parentNode.isTradesDone();
			buildingDone = parentNode.isBuildingDone();
		} else {
			devCardsGenerated = false;
			tradesDone = false;
			buildingDone = false;
		}

	}

	private int getDepth() {
		return depth;
	}

	public void generateChildNodes() {
		// Will we need to consider the number of pieces available?
		ReducedPlayer us = game.getPlayer(ourPlayerNumber);
		int[] resources = us.getResources();

		// Playing Dev Cards

		if (!devCardsGenerated) {
			setDevCardsGenerated(true);
			handlePlayDevCard();
		}

		if (!buildingDone) {
			handleBankTradeChildren(ourPlayerNumber);
		}

		// If we have enough for a road find all the road building locations

		if ((resources[SOCResourceConstants.WOOD - 1] >= 1) && (resources[SOCResourceConstants.CLAY - 1] >= 1)
				&& (game.getOurPlayer().getRoadPieces() > 0)) {
			setBuildingDone(true);
			handleRoadBuild();
		}

		// If we have enough for a settlement find all the locations that we can
		// build it
		if (resources[SOCResourceConstants.WOOD - 1] >= 1 && resources[SOCResourceConstants.CLAY - 1] >= 1
				&& resources[SOCResourceConstants.WHEAT - 1] >= 1 && resources[SOCResourceConstants.SHEEP - 1] >= 1
				&& game.getOurPlayer().getSettlementPieces() > 0) {
			setBuildingDone(true);
			handleSettlementBuild();
		}

		if (resources[SOCResourceConstants.ORE - 1] >= 3 && resources[SOCResourceConstants.WHEAT - 1] >= 2
				&& game.getOurPlayer().getCityPieces() > 0) {
			// BuildCities
			setBuildingDone(true);
			handleCityBuild();
		}

		if (resources[SOCResourceConstants.ORE - 1] >= 1 && resources[SOCResourceConstants.WHEAT - 1] >= 1
				&& resources[SOCResourceConstants.SHEEP - 1] >= 1 && game.getDevCardsLeft() > 0) {
			setBuildingDone(true);
			handleBuyDevCard();
		}

		for (BuildNode child : children) {
			child.getChildren();
		}

	}

	private void handlePlayDevCard() {

		ReducedPlayer us = game.getOurPlayer();
		int[] devCards = us.getDevelopmentCards();
		if (us.getDevCardPlayed()) {
			// If we have already played a dev card then add no child states
			return;
		} else {
			// We have road building
			if (devCards[SOCDevCardConstants.ROADS] > 0 && us.getRoadPieces() >= 2) {
				// Get all the valid 2 road permutations.
				// Do this by getting all valid roads and then all the valid
				// roads on top of that.
				List<Integer> firstLocations = game.getBoard().getLegalRoadLocations(ourPlayerNumber);
				for (Integer firstLocation : firstLocations) {
					ReducedGame gameCopy1 = new ReducedGame(game);
					gameCopy1.getBoard().addRoad(firstLocation, ourPlayerNumber);
					List<Integer> secondLocations = gameCopy1.getBoard().getLegalRoadLocations(ourPlayerNumber);
					for (Integer secondLocation : secondLocations) {
						ReducedGame gameCopy2 = new ReducedGame(gameCopy1);
						gameCopy2.getBoard().addRoad(secondLocation, ourPlayerNumber);
						ReducedPlayer playerCopy = gameCopy2.getOurPlayer();
						playerCopy.setDevCardPlayed(true);
						playerCopy.decrementDevelopmentCards(SOCDevCardConstants.ROADS);
						playerCopy.decrementRoadPieces();
						playerCopy.decrementRoadPieces();
						BotMove move = new PlayRoadBuilding(firstLocation, secondLocation);
						BuildNode child = new BuildNode(gameCopy2, move, this, ourPlayer, referenceGame);
						children.add(child);
					}
				}
			}

			// Year of plenty
			if (devCards[SOCDevCardConstants.DISC] > 0) {
				// Add all permutations of the two resources
				int[] resources = game.getOurPlayer().getResources();

				for (int i = 0; i < resources.length; i++) {
					for (int j = 0; j < resources.length; j++) {
						ReducedGame gameCopy = new ReducedGame(game);
						ReducedPlayer playerCopy = gameCopy.getOurPlayer();
						playerCopy.incrementResource(i);
						playerCopy.incrementResource(j);
						playerCopy.setDevCardPlayed(true);
						playerCopy.decrementDevelopmentCards(SOCDevCardConstants.DISC);
						BotMove move = new PlayYOP(i + 1, j + 1);
						BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
						children.add(child);
					}
				}
			}

			// Year of plenty
			if (devCards[SOCDevCardConstants.MONO] > 0) {
				int[] resources = game.getOurPlayer().getResources();
				for (int i = 0; i < resources.length; i++) {
					// Check each resource
					ReducedGame gameCopy = new ReducedGame(game);
					int resourcesStolen = 0;
					for (ReducedPlayer opponent : gameCopy.getPlayers()) {
						if (opponent.getPlayerNumber() != ourPlayerNumber) {
							resourcesStolen += opponent.getResource(i);
							opponent.setResource(i, 0);
						}
					}
					ReducedPlayer playerCopy = game.getOurPlayer();
					int resource = playerCopy.getResource(i);
					playerCopy.setResource(i, resource + resourcesStolen);
					playerCopy.setDevCardPlayed(true);
					playerCopy.decrementDevelopmentCards(SOCDevCardConstants.MONO);
					BotMove move = new PlayMonopoly(i + 1);
					BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
					children.add(child);
				}
			}

			// We won't put in playing knights atm
			if (devCards[SOCDevCardConstants.KNIGHT] > 0) {

			}

		}

	}

	private void handleBuyDevCard() {
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

	private void handleCityBuild() {

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
			usInCopy.incrementVictoryPoints();
			BotMove move = new PiecePlacement(location, SOCPlayingPiece.CITY);
			BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
			children.add(child);
		}
	}

	private void handleSettlementBuild() {

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
			usInCopy.incrementVictoryPoints();
			BotMove move = new PiecePlacement(location, SOCPlayingPiece.SETTLEMENT);
			BuildNode child = new BuildNode(gameCopy, move, this, ourPlayer, referenceGame);
			children.add(child);
		}
	}

	private void handleRoadBuild() {
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
			// i = what we are trading --Check we haven't received this from the
			// bank before
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

	public boolean isDevCardsGenerated() {
		return devCardsGenerated;
	}

	public void setDevCardsGenerated(boolean devCardsGenerated) {
		this.devCardsGenerated = devCardsGenerated;
	}

	public boolean isTradesDone() {
		return tradesDone;
	}

	public void setTradesDone(boolean tradesDone) {
		this.tradesDone = tradesDone;
	}

	public boolean isBuildingDone() {
		return buildingDone;
	}

	public void setBuildingDone(boolean buildingDone) {
		this.buildingDone = buildingDone;
	}

}
