package misc.bot;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import com.sun.media.sound.SoftLowFrequencyOscillator;

import misc.utils.SettlementResourceInfo;
import soc.client.SOCBoardPanel;
import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCResourceConstants;
import soc.game.SOCSettlement;
import soc.message.SOCGameState;

/**
 * Class that will decide on the initial moves to the game. This is done
 * separately to the normal move decisions because the processing here is quite
 * probabilistic and is not very dynamic. It is easier to simply have some rules
 * to follow and just use those in this case.
 * 
 * @author david
 *
 */
public class InitialMoveDecider {

	// Int showing which turn we first move in
	private int turnOrder;
	private SOCGame game;
	private int strategy;
	private SOCPlayer ourPlayer;
	private SOCBoard board;

	// MAGIC NUMBERS - Weights for various types of decision.
	private static final double BRICK_MUTLIPLIER = 0.90;
	private static final double ORE_MUTLIPLIER = 0.92;
	private static final double ALL_RESOURCE_MULTIPLIER = 0.88;
	private static final double CORRECT_STRAT_MULTIPLIER = 0.85;
	private static final double DOUBLE_RESOURCE_MULTIPLIER = 0.85;

	private static final double INIT = Double.MAX_VALUE;

	/**
	 * Constructor. Initialise this object with information about
	 * 
	 * @param ourPlayer
	 *            The player representing our robot playing the game.
	 */
	public InitialMoveDecider(SOCPlayer ourPlayer) {
		this.ourPlayer = ourPlayer;
		strategy = -1;
	}

	/**
	 * Set the player moving first
	 * 
	 * @param order
	 *            First player number.
	 */
	public void setTurnOrder(int order) {
		this.turnOrder = order;
	}

	/**
	 * Method to take in the state of the game and the board currently given and
	 * to make a decision based upon this information. The result will be 2
	 * digit hex digit coordinate.
	 * 
	 * @param state
	 *            The state the game is currently in
	 * @param board
	 *            The current board layout.
	 * @return The location of the object we are going to board.
	 */
	public int handleDecision(int state, SOCBoard board) {
		int result = 0;
		this.board = board;
		// If we need to build a settlement then build it
		if (state == SOCGame.START1A || state == SOCGame.START2A) {
			result = handleSettlementBuild();

			// If this is the first settlement we are placing then it decides on
			// the strategy we are playing
			if (state == SOCGame.START1A) {
				// decideStrategy(result);
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
	 * first settlement location. These strategies are either to build wood and
	 * brick. Ore and wheat. Or a good mix of all of the elements.
	 * 
	 * TODO this method is too slow by a small margin improve this or change the
	 * server settings.
	 * 
	 * @param location
	 *            The location of the settlement we are building
	 */
	private int decideStrategy(int location) {
		// Get the information of the settlement we are building
		SettlementResourceInfo settlementInfo = new SettlementResourceInfo(location, board);

		ArrayList<Integer> resources = settlementInfo.getResources();
		ArrayList<Integer> values = settlementInfo.getValues();
		double[] resourceCount = new double[] { INIT, INIT, INIT, INIT, INIT };
		int strategy = 3;

		// score each settlement for its resources. Lower is better

		// Loop over the hexes it connects too.
		for (int i = 0; i < resources.size(); i++) {
			double score = Math.abs(7 - values.get(i));
			int value = values.get(i);

			if (resourceCount[value - 1] != INIT) {
				resourceCount[value - 1] = ((resourceCount[value - 1] + score) / 2) * DOUBLE_RESOURCE_MULTIPLIER;
			} else {
				resourceCount[value - 1] = score;
			}
		}

		// Find the lowest values in the array

		double lowestValue = Double.MAX_VALUE;
		double lowestIndex = -1;

		for (int i = 0; i < resources.size(); i++) {
			if (resourceCount[i] < lowestValue) {
				lowestIndex = i;
				lowestValue = resourceCount[i];
			}
		}

		// Add one as they are offset from the game values by one
		lowestIndex++;

		if (lowestValue == SOCResourceConstants.CLAY || lowestValue == SOCResourceConstants.WOOD) {
			strategy = BotBrain.BRICK_STRATEGY;
		} else if (lowestValue == SOCResourceConstants.ORE) {
			strategy = BotBrain.ORE_STRATEGY;
		} else {
			strategy = BotBrain.MIXED_STRATEGY;
		}

		return strategy;
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
	 *            The list of all possible settlements that we can build and
	 *            their associated information
	 */
	private int generateSuggestedSettlement(ArrayList<SettlementResourceInfo> possSetInfo) {
		double bestUtility = Double.MAX_VALUE;
		int bestLocation = 0;

		for (SettlementResourceInfo settlementResourceInfo : possSetInfo) {

			double adjustedUtility = adjustWeighting(settlementResourceInfo);

			if (adjustedUtility < bestUtility) {
				bestUtility = adjustedUtility;
				bestLocation = settlementResourceInfo.getLocation();
			}

		}

		return bestLocation;

	}

	/**
	 * Adjust the weighting score of the settlement based on certain facts and
	 * scores that can be specified in this class. For example we may want it so
	 * that we get bonus weighting for getting a certain resource or a bonus for
	 * getting all resources.
	 * 
	 * @param settlementResourceInfo
	 *            The information of the hexes around a settlement.
	 * @return The adjusted waiting of the score of a settlement
	 */
	private double adjustWeighting(SettlementResourceInfo settlementResourceInfo) {
		boolean containsBrick = false;
		boolean containsOre = false;
		boolean compliesWithStrategy = false;
		boolean allResource = false;
		boolean[] resourcePresent = new boolean[] { false, false, false, false, false };

		ArrayList<Integer> resources = settlementResourceInfo.getResources();

		for (Integer hex : resources) {
			if (hex == SOCResourceConstants.CLAY) {
				containsBrick = true;
			}
			if (hex == SOCResourceConstants.ORE) {
				containsOre = true;
			}
			resourcePresent[hex - 1] = true;
		}

		int totalResources = 0;

		for (boolean resourceType : resourcePresent) {
			if (resourceType == true) {
				totalResources++;
			}
		}

		if (totalResources == 3) {
			allResource = true;
		}

		// If a strategy has been decided upon.
		if (strategy != -1) {
			compliesWithStrategy = checkStrategyAgreement(settlementResourceInfo);
		}

		double multiplier = 1;

		if (containsBrick) {
			multiplier += multiplier * BRICK_MUTLIPLIER;
		}
		if (containsOre) {
			multiplier += multiplier * ORE_MUTLIPLIER;
		}
		if (allResource) {
			multiplier += multiplier * ALL_RESOURCE_MULTIPLIER;
		}

		return multiplier * settlementResourceInfo.getAdjustedResourceWeight();
	}

	/**
	 * Helper method to check whether the second locations are in agreement with
	 * the strategy that is dictated from the first one.
	 * 
	 * @param settlementResourceInfo The location being checked
	 * @return Whether it agrees with our strategy.
	 */
	private boolean checkStrategyAgreement(SettlementResourceInfo settlementResourceInfo) {

		boolean stratAgreement = false;
		ArrayList<Integer> resources = settlementResourceInfo.getResources();
		int[] resourcePresent = new int[] { 0, 0, 0, 0, 0 };

		for (Integer hex : resources) {
			resourcePresent[hex - 1] += 1;
		}

		if (strategy == BotBrain.BRICK_STRATEGY) {
			if ((resourcePresent[SOCResourceConstants.CLAY - 1]
					+ resourcePresent[SOCResourceConstants.WOOD - 1]) >= 2) {
				stratAgreement = true;
			}
		}

		if (strategy == BotBrain.ORE_STRATEGY) {
			if (resourcePresent[SOCResourceConstants.ORE] + resourcePresent[SOCResourceConstants.WHEAT] >= 2) {
				stratAgreement = true;
			}
		}

		if (strategy == BotBrain.MIXED_STRATEGY) {
			int totalResources = 0;

			for (int resourceType : resourcePresent) {
				if (resourceType == 1) {
					totalResources++;
				}
			}

			if (totalResources == 3) {
				stratAgreement = true;
			}
		}

		return stratAgreement;
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
	 * Helper method that returns the location of where a road should be built.
	 * A good idea is to build the road towards the next best settlement.
	 * 
	 * 
	 * @return The location of where a road should be build
	 */
	private int handleRoadBuild() {
		// Get all possible road locations
		ArrayList<Integer> possibleRoadLocations = generatePossibleRoadLocs();

		/**
		 * Get the locations of all of the possible settlements and use this to
		 * move towards a promising area on the map. In the future we could
		 * change this take into account promising locations rather than
		 * individual settlements
		 */
		ArrayList<Integer> possSetlLocations = generatePossibleSetLocs();
		ArrayList<SettlementResourceInfo> possSetInfo = new ArrayList<SettlementResourceInfo>();
		for (Integer location : possSetlLocations) {
			SettlementResourceInfo info = new SettlementResourceInfo(location, board);
			possSetInfo.add(info);
		}

		// Get the location of what we want the next settlement to be
		int settlementLoc = generateSuggestedSettlement(possSetInfo);
		int closestRoad = 0;
		double closestDistance = Double.MAX_VALUE;

		// Euclidean distance *should* work!
		for (Integer roadLocations : possibleRoadLocations) {
			double distance = calcEuclidDistance(roadLocations, settlementLoc);
			if (distance < closestDistance) {
				closestDistance = distance;
				closestRoad = roadLocations;
			}
		}

		return closestRoad;

	}

	/**
	 * Helper method for calculating euclidean between 2 hexadecimal coordinates
	 * on the catan board. This method will not return the actual distance
	 * between them in terms of the Catan board but rather a non square rooted
	 * comparison between the two which will allow you to see which one is
	 * bigger
	 * 
	 * @return
	 */
	private double calcEuclidDistance(int pointA, int pointB) {
		int pointAX = String.valueOf(Integer.toHexString(pointA)).charAt(0);
		int pointAY = String.valueOf(Integer.toHexString(pointA)).charAt(1);
		int pointBX = String.valueOf(Integer.toHexString(pointB)).charAt(0);
		int pointBY = String.valueOf(Integer.toHexString(pointB)).charAt(1);

		int value = (pointAX - pointBX) + (pointAY - pointBY);

		return Math.pow(value, 2);
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
				// System.out.println(String.format("%02X", i));
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

	/**
	 * Set the strategy that we will be playing.
	 * 
	 * @param strategy
	 */
	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	/**
	 * Return the strategy that has been decided on based upon the initial moves
	 * 
	 * @return The strategy that has been chosen.
	 */
	public int getStrategy() {
		return strategy;
	}

}
