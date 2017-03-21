package misc.bot.decisionMakers;

import java.util.*;

import misc.bot.BotBrain;
import misc.bot.buildPlanning.BuildNode;
import misc.bot.moves.BotMove;
import misc.bot.moves.BuyDevCard;
import misc.bot.moves.PlayKnight;
import misc.bot.moves.PlayMonopoly;
import misc.bot.moves.PlayRoadBuilding;
import misc.bot.moves.PlayYOP;
import misc.utils.GameUtils;
import misc.utils.ReducedGame;
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

	protected SOCGame game;
	protected ReducedGame reducedGame;
	protected SOCPlayer ourPlayer;
	public static final int NUMBER_RESOURCES = 5;

	/**
	 * Constructor
	 * 
	 * @param game
	 * @param ourPlayer
	 */
	public DecisionMaker(SOCGame game) {
		this.game = game;
	}

	public void setReducedGame(ReducedGame game) {
		this.reducedGame = new ReducedGame(game);
		reducedGame.setOurPlayerNumber(ourPlayer.getPlayerNumber());
	}

	public void setReducedGame(SOCGame game) {
		this.reducedGame = new ReducedGame(ourPlayer.getPlayerNumber(), game);
		reducedGame.setOurPlayerNumber(ourPlayer.getPlayerNumber());
	}

	public void setOurPlayer(SOCPlayer player) {
		this.ourPlayer = player;
	}

	/**
	 * Update the game state that we are making our decision based on.
	 */
	public void updateGame(SOCGame updatedGame) {
		this.game = updatedGame;
		this.reducedGame = new ReducedGame(ourPlayer.getPlayerNumber(), updatedGame);
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
				List<Integer> knightLocations = getPossibleRobberLocations();
				for (Integer location : knightLocations) {
					// TODO code to figure out who has settlments at which
					// location
					BotMove knightMove = new PlayKnight(location, 0);
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
	protected ArrayList<Integer> getPossibleRobberLocations(boolean b) {
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
				if (!ourCoveredHexes.contains(hexLoc) && game.getBoard().isHexOnLand(hexLoc)
						&& (!(game.getBoard().getHexTypeFromCoord(hexLoc) == SOCBoard.DESERT_HEX)
								&& game.getBoard().getRobberHex() != hexLoc)
						&& game.getBoard().getPreviousRobberHex() != hexLoc) {
					possibleLocations.add(hexLoc);
				}
			}
		}

		for (SOCCity city : allCities) {
			List<Integer> adjHexes = city.getAdjacentHexes();
			for (Integer hexLoc : adjHexes) {
				if (!ourCoveredHexes.contains(hexLoc) && game.getBoard().isHexOnLand(hexLoc)
						&& (!(game.getBoard().getHexTypeFromCoord(hexLoc) == SOCBoard.DESERT_HEX)
								&& game.getBoard().getRobberHex() != hexLoc.intValue())
						&& game.getBoard().getPreviousRobberHex() != hexLoc.intValue()) {
					possibleLocations.add(hexLoc);
				}
			}
		}

		ArrayList<Integer> results = new ArrayList<>(possibleLocations);

		return results;
	}

	protected List<Integer> getPossibleRobberLocations() {
		int currRobber = game.getBoard().getRobberHex();
		int prevRobber = game.getBoard().getPreviousRobberHex();	
		int[] allHexes = game.getBoard().getLandHexCoords();
		List<Integer> possibleHexes = new ArrayList<Integer>();
		Set<Integer> ourConnections = new HashSet<>();
		
		for (SOCCity city : ourPlayer.getCities()) {
			List<Integer> connections = game.getBoard().getAdjacentHexesToNode(city.getCoordinates());
			ourConnections.addAll(connections);
		}
		
		for (SOCSettlement settlement: ourPlayer.getSettlements()) {
			List<Integer> connections = game.getBoard().getAdjacentHexesToNode(settlement.getCoordinates());
			ourConnections.addAll(connections);
		}
		
		
		for (int i = 0; i < allHexes.length; i++) {
			if(!ourConnections.contains(allHexes[i]) && allHexes[i] != currRobber && game.getBoard().getHexTypeFromCoord(allHexes[i])!= SOCBoard.DESERT_HEX){
				possibleHexes.add(allHexes[i]);
			}
		}
		
		return possibleHexes;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<ArrayList<BotMove>> getAllPossibleMoves() {
		ArrayList<ArrayList<BotMove>> moveCombos = new ArrayList<ArrayList<BotMove>>();
		BuildNode root = new BuildNode(reducedGame, null, null, ourPlayer, game);
		root.generateChildNodes();

		List<BuildNode> nodes = new ArrayList<BuildNode>();
		gatherChildren(root, nodes);

		// Generate the move sequences from all the build nodes in the tree
		for (BuildNode buildNode : nodes) {
			moveCombos.add(generateMovesFromNode(buildNode));
		}

		return moveCombos;
	}

	public List<BuildNode> getPossibleChildStates(ReducedGame reducedGame) {
		BuildNode root = new BuildNode(reducedGame, null, null, ourPlayer, game);
		root.generateChildNodes();
		List<BuildNode> nodes = new ArrayList<BuildNode>();
		gatherChildren(root, nodes);
		return nodes;
	}

	public List<BuildNode> getPossibleChildStates(BuildNode buildNode) {
		buildNode.generateChildNodes();
		List<BuildNode> nodes = new ArrayList<BuildNode>();
		gatherChildren(buildNode, nodes);
		return nodes;
	}

	/**
	 * Recursive method to do traversal
	 */
	private void gatherChildren(BuildNode node, List<BuildNode> visited) {
		visited.add(node);
		for (BuildNode childNode : node.getChildren()) {
			gatherChildren(childNode, visited);
		}
	}

	/**
	 * Given build a node gather all the moves that led to it.
	 * 
	 * @param buildNode
	 * @return
	 */
	public ArrayList<BotMove> generateMovesFromNode(BuildNode buildNode) {
		ArrayList<BotMove> moves = new ArrayList<>();
		while (buildNode.getParentNode() != null) {
			moves.add(0, buildNode.getParentMove());
			buildNode = buildNode.getParentNode();
		}
		return moves;
	}
	
	public static  ArrayList<BotMove> generateMovesFromNode(BuildNode buildNode,boolean b) {
		ArrayList<BotMove> moves = new ArrayList<>();
		while (buildNode.getParentNode() != null) {
			moves.add(0, buildNode.getParentMove());
			buildNode = buildNode.getParentNode();
		}
		return moves;
	}
	
	@Deprecated
	public static ArrayList<BotMove> genMoves(BuildNode buildNode) {
		ArrayList<BotMove> moves = new ArrayList<>();
		while (buildNode.getParentNode() != null) {
			moves.add(0, buildNode.getParentMove());
			buildNode = buildNode.getParentNode();
		}
		return moves;
	}

	/**
	 * Helper method returns all the resources that a player has.
	 * 
	 * @return
	 */
	protected int[] getOurResources() {
		return reducedGame.getOurPlayer().getResources();
	}

	public int getOurPlayerNumber() {
		return ourPlayer.getPlayerNumber();
	}

	/**
	 * Abstract method that must be implemented. Return a list of
	 * {@link BotMove} that should be executed by the {@link BotBrain}.
	 * 
	 * @return A list of moves that should be implemented in the coming turn.
	 */
	public abstract List<BotMove> getMoveDecision();

	public abstract int getNewRobberLocation();

	public abstract int[] getRobberDiscard(int discards);

	public abstract int getRobberTarget();

}
