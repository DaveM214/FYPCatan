package misc.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import misc.bot.moves.PiecePlacement;
import misc.bot.moves.PlayKnight;
import misc.bot.moves.PlayMonopoly;
import misc.bot.moves.PlayRoadBuilding;
import misc.bot.moves.PlayYOP;
import misc.bot.moves.Trade;
import soc.game.SOCBoard;
import soc.game.SOCDevCardConstants;
import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCPlayingPiece;
import sun.security.util.Length;

/**
 * Class representing the basic information about a Game.
 * 
 * @author david
 *
 */
public class ReducedGame {

	private SOCGame referenceGame;
	private final int ourPlayerNumber;
	private ReducedBoard board;
	private int devCardsLeft;
	private List<ReducedPlayer> players;
	private List<Integer> devCards;

	public final static int WINNING_VP = 10;
	public static final int MAX_DEV_CARDS = 25;
	public static final int MAX_KNIGHTS = 14;
	public static final int MAX_VP = 5;
	public static final int MAX_ROAD_BUILDING = 2;
	public static final int MAX_MONOPOLY = 2;
	public static final int MAX_YOP = 2;

	/**
	 * Constructor. Creates a reduced game from an existing SOCGame;
	 * 
	 * @param ourPlayerNumber
	 * @param game
	 */
	public ReducedGame(int ourPlayerNumber, SOCGame game) {

		this.ourPlayerNumber = ourPlayerNumber;
		this.devCardsLeft = game.getNumDevCards();
		this.board = new ReducedBoard(game.getBoard());
		this.referenceGame = game;

		SOCPlayer[] socPlayers = game.getPlayers();
		players = new ArrayList<ReducedPlayer>();
		for (SOCPlayer socPlayer : socPlayers) {
			ReducedPlayer reducedPlayer = new ReducedPlayer(socPlayer);
			players.add(reducedPlayer);
		}

		System.out.println("Reduced Game Resources: " + players.get(ourPlayerNumber).getResources().toString());

	}

	/**
	 * Copy constructor, create a new ReducedGame as a copy of the one passed in
	 * the to the parameters.
	 * 
	 * @param orig
	 */
	public ReducedGame(ReducedGame orig) {
		this.ourPlayerNumber = orig.getOurPlayerNumber();
		this.devCardsLeft = orig.getDevCardsLeft();
		this.board = new ReducedBoard(orig.getBoard());
		players = new ArrayList<ReducedPlayer>();
		for (ReducedPlayer player : orig.getPlayers()) {
			ReducedPlayer newPlayer = new ReducedPlayer(player);
			players.add(newPlayer);
		}
		this.referenceGame = referenceGame;

	}

	/**
	 * Get our player number
	 * 
	 * @return Our player number
	 */
	public int getOurPlayerNumber() {
		return this.ourPlayerNumber;
	}

	/**
	 * Get the number of remaining dev cards that can be bought
	 * 
	 * @return
	 */
	public int getDevCardsLeft() {
		return devCardsLeft;
	}

	/**
	 * Set the number of remaining dev cards that can be bought
	 * 
	 * @param cardsLeft
	 */
	public void SetDevCardsLeft(int cardsLeft) {
		this.devCardsLeft = cardsLeft;
	}

	/**
	 * Get the simple board for the game.
	 * 
	 * @return The reduced game board
	 */
	public ReducedBoard getBoard() {
		return this.board;
	}

	/**
	 * Get the object representing our simplified player.
	 * 
	 * @return Our reduced player.
	 */
	public ReducedPlayer getOurPlayer() {
		return players.get(ourPlayerNumber);
	}

	public List<ReducedPlayer> getPlayers() {
		return players;
	}

	/**
	 * Decrement the number of dev cards left.
	 */
	public void decrementDevCards() {
		devCardsLeft--;
	}

	/**
	 * Returns -1 if the game is ongoing and the number of the winning player if
	 * the correct number of victory points have been reached.
	 * 
	 * @return
	 */
	public int isFinished() {
		int winner = -1;
		for (ReducedPlayer reducedPlayer : getPlayers()) {
			if (reducedPlayer.getVictoryPoints() >= WINNING_VP) {
				winner = reducedPlayer.getPlayerNumber();
			}
		}
		return winner;
	}

	/**
	 * Get the player sitting at a specific seat. 0-3
	 * 
	 * @param player
	 *            The play number of the player we want
	 * @return The reduced player object referred to by the number.
	 */
	public ReducedPlayer getPlayer(int player) {
		return players.get(player);
	}

	/**
	 * Check whether the game is finished. If any of the players have reached
	 * the required number of victory points then we will assume that it is
	 * over.
	 * 
	 * @return Whether the game is over.
	 */
	public boolean isGameFinished() {
		for (ReducedPlayer reducedPlayer : players) {
			if (reducedPlayer.getVictoryPoints() == WINNING_VP) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Given a roll of the dice assign the correct resources to all of the
	 * players.
	 * 
	 * @param roll
	 *            The value that has been rolled.
	 */
	public void assignResources(int roll) {
		SOCBoard refBoard = board.getReferenceBoard();
		int[] allHexes = refBoard.getLandHexCoords();
		List<Integer> matchingHexes = new ArrayList<Integer>();

		for (int i = 0; i < allHexes.length; i++) {
			if (refBoard.getNumberOnHexFromCoord(allHexes[i]) == roll) {
				matchingHexes.add(allHexes[i]);
			}
		}

		List<ReducedBoardPiece> settlements = board.getSettlements();
		List<ReducedBoardPiece> cities = board.getCities();

		List<Integer> presentNodes = new ArrayList<Integer>();
		List<Integer> resourceType = new ArrayList<Integer>();

		// Get a list of the nodes around the matching hexes
		for (Integer coord : matchingHexes) {
			int[] nodes = refBoard.getAdjacentNodesToHex(coord);
			int rType = refBoard.getHexTypeFromCoord(coord);
			for (int i : nodes) {
				presentNodes.add(i);
				resourceType.add(rType);
			}
		}

		for (int i = 0; i < presentNodes.size(); i++) {
			ReducedSettlement sett = board.getSettlementAtLocation(presentNodes.get(i));
			if (sett != null) {
				int owner = sett.getOwner();
				players.get(owner).incrementResource(resourceType.get(i) - 1);
			}

			ReducedCity city = board.getCityAtLocation(presentNodes.get(i));
			if (city != null) {
				int owner = city.getOwner();
				players.get(owner).incrementResource(resourceType.get(i) - 1);
				players.get(owner).incrementResource(resourceType.get(i) - 1);
			}
		}

	}

	public void handleMonopoly(PlayMonopoly mono, int player) {
		int res = mono.getTargetResource();
		int stolenResource = 0;

		for (ReducedPlayer reducedPlayer : players) {
			if (reducedPlayer.getPlayerNumber() != player) {
				int i = reducedPlayer.getResource(res);
				stolenResource += i;
				reducedPlayer.setResource(res, 0);
			}
		}

		int currResource = players.get(player).getResource(res);
		players.get(player).setResource(res, stolenResource + currResource);
		players.get(player).decrementDevelopmentCards(SOCDevCardConstants.MONO);
	}

	public void handleTrade(Trade trade, int player) {
		if (trade.getTradeTarget() == -1) {
			int giveAmt = trade.getGiveAmount();
			int recAmt = trade.getRecAmount();
			int giveType = trade.getGiveType() - 1;
			int recType = trade.getRecType() - 1;

			for (int i = 0; i < giveAmt; i++) {
				players.get(player).decrementResource(giveType);
			}

			for (int i = 0; i < recAmt; i++) {
				players.get(player).incrementResource(giveType);
			}
		}
	}

	public void handleYOP(PlayYOP yop, int player) {
		int resource1 = yop.getResource1() - 1;
		int resource2 = yop.getResource2() - 1;
		players.get(player).incrementResource(resource1);
		players.get(player).incrementResource(resource2);
		players.get(player).decrementDevelopmentCards(SOCDevCardConstants.DISC);
	}

	/**
	 * Hand a player using a road building card and building two roads.
	 * 
	 * @param roadBuild
	 *            The details of the roads being built
	 * @param player
	 *            The player building the roads.
	 */
	public void handleRoadBuilding(PlayRoadBuilding roadBuild, int player) {
		int location1 = roadBuild.getLoc1();
		int location2 = roadBuild.getLoc2();
		board.addRoad(location1, player);
		board.addRoad(location2, player);
		players.get(player).decrementRoadPieces();
		players.get(player).decrementRoadPieces();
		players.get(player).decrementDevelopmentCards(SOCDevCardConstants.MONO);
	}

	/**
	 * Handle a player wanting to play a knight card in the game. This will
	 * involve moving the robber and then getting a random resource from the
	 * player.
	 * 
	 * @param knight
	 * @param player
	 */
	public void handleKnightCard(PlayKnight knight, int player) {
		players.get(player).decrementDevelopmentCards(SOCDevCardConstants.KNIGHT);
		handleMoveRobber(knight.getTargetHex(),player,knight.getTargetPlayer());
	}
	
	public void handleMoveRobber(int location, int player,int target){
		board.setRobberLocation(location);
		int stolenResource = (players.get(target).robPlayer());
		players.get(player).incrementResource(stolenResource);
	}

	/**
	 * Handle buying development card.
	 * 
	 * @param player
	 *            The player that is buying the development card.
	 */
	public void handleBuyingDevCard(int player) {
		if (devCards != null) {
			int card = devCards.remove(0);
			ReducedPlayer owner = players.get(player);
			owner.getDevelopmentCards()[card]++;
			devCardsLeft--;
		} else {
			System.out.println("Dev cards not initialised");
		
		}
	}

	/**
	 * Handle a player wanting to build a piece in the game.
	 * 
	 * @param build
	 *            The thing the want to build and where
	 * @param player
	 *            The id number of the player that is building it.
	 */
	public void handlePiecePlacement(PiecePlacement build, int player) {
		int type = build.getPieceType();
		switch (type) {
		case SOCPlayingPiece.ROAD:
			board.addRoad(build.getCoordinate(), player);
			players.get(player).decrementRoadPieces();
			break;
		case SOCPlayingPiece.CITY:
			board.addCity(build.getCoordinate(), player);
			players.get(player).decrementCityPieces();
			players.get(player).incrementSettlementPieces();
			players.get(player).incrementVictoryPoints();
			break;
		case SOCPlayingPiece.SETTLEMENT:
			board.addSettlement(build.getCoordinate(), player);
			players.get(player).decrementSettlementPieces();
			players.get(player).incrementVictoryPoints();
			break;
		}

	}

	/**
	 * Create a deck of development cards to use based on a samping of those
	 * that should be left;
	 */
	public void createDevCardDeck() {
		double propRemaining = MAX_DEV_CARDS / getDevCardsLeft();
		Double currVictory = propRemaining * MAX_VP;
		Double currKnights = propRemaining * MAX_KNIGHTS;
		Double curr2Card = propRemaining * MAX_YOP;
		int ourSum = currVictory.intValue() + currKnights.intValue() + (curr2Card.intValue() * 3);
		int difference = getDevCardsLeft() - ourSum;
		currKnights = currKnights + difference;
		devCards = new ArrayList<Integer>();

		for (int i = 0; i < currKnights.intValue(); i++) {
			devCards.add(SOCDevCardConstants.KNIGHT);
		}
		for (int i = 0; i < currVictory.intValue(); i++) {
			devCards.add(SOCDevCardConstants.CAP);
		}
		for (int i = 0; i < curr2Card.intValue(); i++) {
			devCards.add(SOCDevCardConstants.DISC);
			devCards.add(SOCDevCardConstants.ROADS);
			devCards.add(SOCDevCardConstants.MONO);
		}

		Collections.shuffle(devCards);

	}

	public void handlePlayerDiscard(int[] discarded, int player) {
		ReducedPlayer discardingPlayer = players.get(player);
		for(int i=0;i<discarded.length;i++){
			int amount = discarded[i];
			for(int j = 0; j < amount;j++){
				discardingPlayer.decrementResource(i);
			}
		}
	}

}
