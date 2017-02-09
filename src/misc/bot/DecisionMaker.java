package misc.bot;

import java.util.*;

import misc.bot.buildPlanning.BuildNode;
import misc.bot.moves.BotMove;
import misc.bot.moves.BuyDevCard;
import misc.bot.moves.PlayKnight;
import misc.bot.moves.PlayMonopoly;
import misc.bot.moves.PlayRoadBuilding;
import misc.bot.moves.PlayYOP;
import soc.game.SOCBoard;
import soc.game.SOCCity;
import soc.game.SOCDevCardConstants;
import soc.game.SOCGame;
import soc.game.SOCInventory;
import soc.game.SOCInventoryItem;
import soc.game.SOCPlayer;
import soc.game.SOCPlayingPiece;
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;
import soc.game.SOCRoad;
import soc.game.SOCSettlement;

/**
 * This is the class than when provided with the current board available in an
 * {@link SOCGame} will formulate a series of moves than will be played. This
 * class is abstract and provides utilities available for a decision making AI
 * class that wishes to search or otherwise work out what move to play. How this
 * information is utilised is left to the individual classes that extend this
 * one.
 * 
 * @author david
 *
 */
public abstract class DecisionMaker {

	private SOCGame game;
	private SOCPlayer ourPlayer;
	private static final int NUMBER_RESOURCES = 5;

	/**
	 * Constructor
	 * 
	 * @param game
	 * @param ourPlayer
	 */
	public DecisionMaker(SOCGame game, SOCPlayer ourPlayer) {
		this.game = game;
		this.ourPlayer = ourPlayer;
	}

	/**
	 * Update the game state that we are making our decision based on.
	 */
	public void updateGame(SOCGame updatedGame) {
		game = updatedGame;
	}

	/**
	 * Method to indicate which player in the game is us and set the field which
	 * stores this information
	 * 
	 * @param ourPlayer
	 *            Our player information
	 */
	public void setOurPlayerInformation(SOCPlayer ourPlayer) {
		this.ourPlayer = ourPlayer;
	}

	/**
	 * Method works out what moves are possible and returns a list of all the
	 * lists of moves.
	 */
	public ArrayList<ArrayList<BotMove>> getAllPossibleMoves() {
		ArrayList<ArrayList<BotMove>> possibles = new ArrayList<ArrayList<BotMove>>();

		ArrayList<BotMove> devCardsMoves = getDevCardMoves();
		ArrayList<BotMove> bankTradeMoves = getBankTradeMoves();
		ArrayList<ArrayList<BotMove>> buildMoves = getBuildMoves();

		// Check if we can play development cards.
		// Go through cards see which are playable

		// Check if we can trade with the bank
		// Do this first as it frees up resources.

		// Check if we can build any roads and settlements or buy development
		// cards.

		// Combine these into the required permutations

		// Don't worry about ordering as end result is the same as long as you
		// are doing stuff that can increase the possible resources first.

		return possibles;
	}

	/**
	 * Get all the possible moves that involve playing development cards.
	 * 
	 * @return The list of all possible moves from paying development cards.
	 */
	private ArrayList<BotMove> getDevCardMoves() {
		SOCInventory inv = ourPlayer.getInventory();
		List<SOCInventoryItem> playableCards = inv.getByState(SOCInventory.PLAYABLE);

		ArrayList<BotMove> devCardMoves = new ArrayList<BotMove>();

		for (SOCInventoryItem card : playableCards) {
			switch (card.itype) {

			// Knight Card
			case SOCDevCardConstants.KNIGHT:
				ArrayList<Integer> knightLocations = getPossibleRobberLocations();
				for (Integer location : knightLocations) {
					BotMove knightMove = new PlayKnight(location);
					devCardMoves.add(knightMove);
				}
				break;

			// Year of plenty - all possible combinations of two resources
			case SOCDevCardConstants.DISC:

				for (int i = 1; i <= NUMBER_RESOURCES; i++) {
					for (int j = 1; j <= NUMBER_RESOURCES; j++) {
						BotMove yearOfPlenty = new PlayYOP(i, j);
						devCardMoves.add(yearOfPlenty);
					}
				}

				break;

			// Road building
			case SOCDevCardConstants.ROADS:
				ArrayList<PlayRoadBuilding> roadLocations = getDoubleRoadLocations();
				devCardMoves.addAll(roadLocations);
				break;

			// Monopoly
			case SOCDevCardConstants.MONO:
				for (int i = 1; i <= NUMBER_RESOURCES; i++) {
					BotMove monopoly = new PlayMonopoly(i);
					devCardMoves.add(monopoly);
				}
				break;

			default:
				break;
			}
		}

		return devCardMoves;
	}

	/**
	 * Helper method. Return all combinations of locations that we can build two
	 * roads in a row with.
	 * 
	 * @return All possible locations where we can build two roads encapsulated
	 *         as a bot move.
	 */
	private ArrayList<PlayRoadBuilding> getDoubleRoadLocations() {
		int minEdge = 0x22;
		int maxEdge = 0xCC;

		ArrayList<Integer> roadLocs = new ArrayList<Integer>();

		// Loop through all coordinates that may be buildable edges
		for (int i = minEdge; i <= maxEdge; i++) {
			if (ourPlayer.isPotentialRoad(i) && ourPlayer.isLegalRoad(i)) {
				// If the coordinate is a viable road add it to the list.
				roadLocs.add(i);
			}
		}

		ArrayList<PlayRoadBuilding> possMoves = new ArrayList<PlayRoadBuilding>();

		// What roads already exist on the game board?
		List<SOCRoad> presentRoads = game.getBoard().getRoads();
		List<Integer> presentRoadLocations = new ArrayList<Integer>();
		for (SOCRoad socRoad : presentRoads) {
			presentRoadLocations.add(socRoad.getCoordinates());
		}

		// We now have all the locations for the possible first road.
		for (Integer possibleFirstLocation : roadLocs) {
			List<Integer> possibleSecondLocations = new ArrayList<Integer>();

			// All possible locations of the first road are still possible minus
			// the one we added.
			for (Integer firstLocs : roadLocs) {
				if (!(firstLocs.equals(possibleFirstLocation))) {
					possibleSecondLocations.add(firstLocs.intValue());
				}
			}

			// Get all the new roads (2 at most) that are possible as a result
			// of the first
			// one and add them to the set of possible second locations if they
			// would be legal. They are not legal if there is road already there
			List<Integer> adjacentTofirst = game.getBoard().getAdjacentEdgesToEdge(possibleFirstLocation);

			for (Integer possSecond : adjacentTofirst) {
				// If it is not already in the list and not already occupied add
				// it to the list
				if (!(presentRoadLocations.contains(possSecond) && !(possibleSecondLocations.contains(possSecond)))) {
					possibleSecondLocations.add(possSecond);
				}
			}

			// Create objects that can be returned
			for (Integer possibleSecondLocation : possibleSecondLocations) {
				PlayRoadBuilding possibleDoubleBuild = new PlayRoadBuilding(possibleFirstLocation,
						possibleSecondLocation);
				possMoves.add(possibleDoubleBuild);
			}

		}

		// All possible second locations will be first less the placed road plus
		// the possible additions of the placed road.
		return possMoves;
	}

	/**
	 * All the possible locations of places that we could move the robber too.
	 * We will not include locations of the robber where we have the possibility
	 * of robbing ourselves
	 * 
	 * @return
	 */
	private ArrayList<Integer> getPossibleRobberLocations() {
		// Find all hexes our settlements touch
		// Get all touching hexes. Remove ours from list.
		// All the remaining hexes are possible places to place it.

		Vector<SOCSettlement> ourSettlements = ourPlayer.getSettlements();
		Vector<SOCCity> ourCities = ourPlayer.getCities();
		Set<Integer> ourCoveredHexes = new TreeSet<Integer>();

		for (SOCCity socCity : ourCities) {
			List<Integer> ourCityLocs = socCity.getAdjacentHexes();
			for (Integer location : ourCityLocs) {
				ourCoveredHexes.add(location);
			}
		}

		for (SOCSettlement socSettlement : ourSettlements) {
			List<Integer> ourSettlementLocs = socSettlement.getAdjacentHexes();
			for (Integer location : ourSettlementLocs) {
				ourCoveredHexes.add(location);
			}
		}

		// We have all the location we touch now.

		// Get all settlements and cities
		List<SOCSettlement> allSettlements = game.getBoard().getSettlements();
		List<SOCCity> allCities = game.getBoard().getCities();

		Set<Integer> possibleLocations = new TreeSet<Integer>();

		// Add all adjacent hexes that we are not adjacent too as we do not want
		// to rob ourselves

		for (SOCSettlement settlement : allSettlements) {
			List<Integer> adjHexes = settlement.getAdjacentHexes();
			for (Integer hexLoc : adjHexes) {
				if (!ourCoveredHexes.contains(hexLoc)) {
					possibleLocations.add(hexLoc);
				}
			}
		}

		for (SOCCity city : allCities) {
			List<Integer> adjHexes = city.getAdjacentHexes();
			for (Integer hexLoc : adjHexes) {
				if (!ourCoveredHexes.contains(hexLoc)) {
					possibleLocations.add(hexLoc);
				}
			}
		}

		ArrayList<Integer> results = new ArrayList<>(possibleLocations);

		return results;
	}

	/**
	 * TODO complete this method. Get all the possible bank trade types of
	 * moves.
	 * 
	 * @return
	 */
	private ArrayList<BotMove> getBankTradeMoves() {
		return null;
	}

	/**
	 * Return all of the things that we can build this includes dev cards that
	 * we can buy.
	 * 
	 * @return
	 */
	private ArrayList<ArrayList<BotMove>> getBuildMoves() {
		// Find all the things we can build
		ArrayList<ArrayList<BotMove>> possibleMoves = new ArrayList<ArrayList<BotMove>>();

		// See if we have the correct resources to build anything
		SOCResourceSet resources = ourPlayer.getResources();

		// We could use an Array but it is slightly more readable to
		// individually declare the resources
		int numClay = resources.getAmount(SOCResourceConstants.CLAY);
		int numWood = resources.getAmount(SOCResourceConstants.WOOD);
		int numOre = resources.getAmount(SOCResourceConstants.ORE);
		int numWheat = resources.getAmount(SOCResourceConstants.WHEAT);
		int numSheep = resources.getAmount(SOCResourceConstants.SHEEP);

		// Work out if anything is buildable with these.
		// We need to consider both the resource and the location available.
		ArrayList<ArrayList<BotMove>> possibleBuilds = checkForBuilds(numClay, numWood, numOre, numWheat, numSheep);

		return null;
	}

	/**
	 * Work out if it is possible to put the piece down.
	 * 
	 * @param numClay
	 * @param numWood
	 * @param numOre
	 * @param numWheat
	 * @param numSheep
	 * @return
	 */
	private ArrayList<ArrayList<BotMove>> checkForBuilds(int numClay, int numWood, int numOre, int numWheat,
			int numSheep) {
		
		BuildNode root = new BuildNode(numSheep, numSheep, numSheep, numSheep, numSheep, null, null, ourPlayer, game);

		return null;

	}

	/**
	 * Abstract method that must be implemented. Return a list of
	 * {@link BotMove} that should be executed by the {@link BotBrain}.
	 * 
	 * @return A list of moves that should be implemented in the coming turn.
	 */
	public abstract ArrayList<BotMove> getMoveDecision();

}
