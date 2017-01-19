package misc.bot;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import misc.utils.SettlementResourceInfo;
import soc.client.SOCBoardPanel;
import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCSettlement;
import soc.message.SOCGameState;

public class InitialMoveDecider {

	// Int showing which turn we first move in
	private int turnOrder;
	private SOCGame game;
	private int strategy;
	private SOCPlayer ourPlayer;
	private SOCBoard board;

	public InitialMoveDecider(SOCPlayer ourPlayer) {
		this.ourPlayer = ourPlayer;
	}

	public void setTurnOrder(int order) {
		this.turnOrder = order;
	}

	public int handleDecision(int state, SOCBoard board) {
		int result = 0;
		this.board = board;
		// If we need to build a settlement then build it
		if (state == SOCGame.START1A || state == SOCGame.START2A) {
			result = handleSettlementBuild();
			if (state == SOCGame.START1A) {
				decideStrategy(result);
			}

			// Otherwise we are building a road
		} else {
			result = handleRoadBuild();
			System.out.println("BUILDING AT: " + String.format("%02X", result));
		}

		return result;
	}

	/**
	 * Work out the strategy that we will be playing with once we work out the
	 * first settlement location.
	 * 
	 * @param location
	 *            The location of the settlement we are building
	 */
	private void decideStrategy(int location) {
		return;
	}

	/**
	 * Helper method that returns the location of where a settlement should be
	 * built
	 * 
	 * @return The location of where a settlement should be build
	 */
	private int handleSettlementBuild() {
		// Get all possible locations
		ArrayList<Integer> possSetlLocations = generatePossibleSetLocs();
		ArrayList<SettlementResourceInfo> possSetInfo = new ArrayList<SettlementResourceInfo>();
	
		// Generate all of the information for the possible settlements
		for (Integer location : possSetlLocations) {	
			SettlementResourceInfo info = new SettlementResourceInfo(location, board);
			possSetInfo.add(info);	
		}

		int settlementLocation = generateSuggestedSettlement(possSetInfo);

		return settlementLocation;
	}

	/**
	 * Given a list of all the possible settlements that we can build
	 * 
	 * @param possSetInfo
	 */
	private int generateSuggestedSettlement(ArrayList<SettlementResourceInfo> possSetInfo) {
		Random rand = new Random();
		int randInt = rand.nextInt(possSetInfo.size());
		return possSetInfo.get(randInt).getLocation();
	}

	/**
	 * Method to calculate all of the available locations of the settlements.
	 * These will be all of the locations that are not already occupied by an
	 * already settlement and are not too close to one that already exists.
	 * 
	 * @return List of all possible settlement locations
	 */
	private ArrayList<Integer> generatePossibleSetLocs() {
		int[] locations = ourPlayer.getPotentialSettlements_arr();
		ArrayList<Integer> locList = new ArrayList<Integer>();

		for (int location : locations) {
			locList.add(location);
		}

		return locList;
	}

	/**
	 * Helper method that returns the location of where a road should be built
	 * 
	 * @return The location of where a road should be build
	 */
	private int handleRoadBuild() {
		// Get all possible road locations
		ArrayList<Integer> possibleRoadLocations = generatePossibleRoadLocs();

		// Choose new road location
		int roadLocation = generateSugggestedRoadLocation(possibleRoadLocations);
		return roadLocation;
	}

	/**
	 * Helper method, given a list of all of the possible locations a road can
	 * be built pick one.
	 * 
	 * @param possibleRoadLocations
	 * @return
	 */
	private int generateSugggestedRoadLocation(ArrayList<Integer> possibleRoadLocations) {
		// TODO do this properly. Just do it randomly now.
		Random rand = new Random();
		int randInt = rand.nextInt(possibleRoadLocations.size());
		return (possibleRoadLocations.get(randInt));
	}

	/**
	 * Method that will generate all of the possible coordinates of the roads
	 * that we can build. This list will be a lot shorter than that containing
	 * all of the possible settlements as a road must be connected to one of the
	 * settlements that we have built.
	 * 
	 * We will do this by going through all the coordinates for edges and
	 * querying if they are valid road building locations.
	 * 
	 * @return List of all the possible locations of the roads
	 */
	private ArrayList<Integer> generatePossibleRoadLocs() {
		// Hex values representing the board
		// TODO extract these to a proper constant
		int minEdge = 0x22;
		int maxEdge = 0xCC;

		ArrayList<Integer> roadLocs = new ArrayList<Integer>();

		// Loop through all coordinates that may be buildable edges
		for (int i = minEdge; i <= maxEdge; i++) {
			if (ourPlayer.isPotentialRoad(i) && ourPlayer.isLegalRoad(i)) {
				// If the coordinate is a viable road add it to the list.
				roadLocs.add(i);
				//System.out.println(String.format("%02X", i));
			}
		}
		return roadLocs;
	}

	/**
	 * Set the internal representation of the game and the state which we will
	 * make the decisions based upon
	 * 
	 * @param game
	 */
	public void setGame(SOCGame game) {
		this.game = game;
	}

	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

}
