package misc.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import soc.game.SOCBoard;
import soc.game.SOCCity;
import soc.game.SOCRoad;
import soc.game.SOCSettlement;

/**
 * More workable class for search and simulation that uses a cut down version of
 * the board that is faster and easier to work with in terms of making copies of
 * various items. In this particular representation we won't care about the
 * hexes and their resources as this can always be referred back to the original
 * board
 * 
 * @author david
 *
 */
public class ReducedBoard {

	private List<ReducedBoardPiece> roads;
	private List<ReducedBoardPiece> settlements;
	private List<ReducedBoardPiece> cities;

	private SOCBoard referenceBoard;

	public static final int MIN_SETTLEMENT = 0x23;
	public static final int MAX_SETTLEMENT = 0xDC;
	public static final int MIN_HEX = 0x33;
	public static final int MAX_HEX = 0xBB;
	public static final int MIN_ROAD = 0x22;
	public static final int MAX_ROAD = 0xCC;

	/**
	 * Constructor that creates a reduced board from a regular SOCBoard. Copy
	 * roads etc. from the previous board.
	 * 
	 * @param board
	 */
	public ReducedBoard(SOCBoard board) {
		roads = new ArrayList<>();
		settlements = new ArrayList<>();
		cities = new ArrayList<>();
		this.referenceBoard = board;

		List<SOCRoad> socRoads = board.getRoads();
		List<SOCSettlement> socSettlements = board.getSettlements();
		List<SOCCity> socCities = board.getCities();

		for (SOCRoad socRoad : socRoads) {
			roads.add(new ReducedRoad(socRoad.getCoordinates(), socRoad.getPlayerNumber()));
		}

		for (SOCSettlement socSettlement : socSettlements) {
			settlements.add(new ReducedSettlement(socSettlement.getCoordinates(), socSettlement.getPlayerNumber()));
		}

		for (SOCCity socCity : socCities) {
			cities.add(new ReducedCity(socCity.getCoordinates(), socCity.getPlayerNumber()));
		}

	}

	/**
	 * Copy constructor. Makes a copy of a reduced board passed in as a
	 * parameter.
	 * 
	 * @param orig
	 */
	public ReducedBoard(ReducedBoard orig) {
		roads = new ArrayList<ReducedBoardPiece>();
		settlements = new ArrayList<ReducedBoardPiece>();
		cities = new ArrayList<ReducedBoardPiece>();
		referenceBoard = orig.referenceBoard;

		for (ReducedBoardPiece city : orig.getCities()) {
			ReducedCity newCity = new ReducedCity(city);
			cities.add(newCity);
		}

		for (ReducedBoardPiece road : orig.getRoads()) {
			ReducedRoad newRoad = new ReducedRoad(road);
			roads.add(newRoad);
		}

		for (ReducedBoardPiece settlement : orig.getSettlements()) {
			ReducedSettlement newSettlement = new ReducedSettlement(settlement);
			settlements.add(newSettlement);
		}

	}

	/**
	 * Return a list of all the locations that a player (passed as the
	 * parameter) could build a settlement if they had the resources and the
	 * available pieces. The way this is done is by looping over the build nodes
	 * and seeing if they are viable for a player. TODO future task may be to
	 * hold a consistent list rather than calculate this dynamically
	 * 
	 * @param player
	 *            The player that the list is for
	 * @return The locations that a player can build a settlement.
	 */
	public List<Integer> getLegalSettlementLocations(int player) {
		// Settlements can only be built on roads so find all the roads a player
		// has.
		List<ReducedBoardPiece> playersRoads = getPlayersRoads(player);
		List<Integer> coveredNodes = new ArrayList<Integer>();

		for (ReducedBoardPiece road : playersRoads) {
			coveredNodes.addAll(referenceBoard.getAdjacentNodesToEdge(road.getLocation()));
		}

		// Remove duplicate nodes
		Set<Integer> nodes = new HashSet<Integer>(coveredNodes);
		List<Integer> validSettlements = new ArrayList<Integer>();

		// We have all the possible locations that we can build
		for (Integer settlementLocation : nodes) {
			if (isValidSettlement(settlementLocation, player)) {
				validSettlements.add(settlementLocation);
			}
		}

		return validSettlements;

	}

	private List<ReducedBoardPiece> getPlayersRoads(int player) {
		List<ReducedBoardPiece> playersRoads = new ArrayList<ReducedBoardPiece>();
		for (ReducedBoardPiece reducedRoad : roads) {
			if (reducedRoad.getOwner() == player) {
				playersRoads.add(reducedRoad);
			}
		}
		return playersRoads;
	}

	public List<Integer> getLegalRoadLocations(int player) {
		List<ReducedBoardPiece> playersRoads = getPlayersRoads(player);
		Set<Integer> possibleLocations = new HashSet<Integer>();
		
		//Add all connecting edges
		for (ReducedBoardPiece road : playersRoads) {
			possibleLocations.addAll(referenceBoard.getAdjacentEdgesToEdge(road.getLocation()));
		}
		
		for (ReducedBoardPiece road: playersRoads){
			int location = road.getLocation();
			if(possibleLocations.contains(location)){
				possibleLocations.remove(location);
			}
		}
		
		return new ArrayList<Integer>(possibleLocations);	
	}

	/**
	 * Return the list of the legal locations that a player could build a city.
	 * This method does not take into account the number of playing pieces that
	 * a player has left.
	 * 
	 * @param player
	 *            The player building the city
	 * @return List of locations that a city could be built. Empty if there are
	 *         no permissible locations.
	 */
	public List<Integer> getLegalCityLocations(int player) {
		List<Integer> locations = new ArrayList<Integer>();

		// Valid city locations are where ever we have a settlement
		for (ReducedBoardPiece settlement : settlements) {
			if (settlement.getOwner() == player) {
				locations.add(settlement.getLocation());
			}
		}

		return locations;
	}

	/**
	 * Return true if the location is legal for a specific player to build a
	 * road on and false if not.
	 * 
	 * @param location
	 * @param owner
	 * @return
	 */
	public boolean isValidRoad(int location, int owner) {
		return false;
	}

	/**
	 * Return true if the location is legal for a specific player to build a
	 * settlement on and false if it is not.
	 * 
	 * @param location
	 * @param owner
	 * @return
	 */
	public boolean isValidSettlement(int location, int owner) {
		List<Integer> surroundingNodes = referenceBoard.getAdjacentNodesToNode(location);
		List<Integer> surroundingEdges = referenceBoard.getAdjacentEdgesToNode(location);

		List<ReducedBoardPiece> pieces = new ArrayList<>(settlements);
		pieces.addAll(cities);

		for (ReducedBoardPiece piece : pieces) {
			// If there is a piece too close then not valid
			if (surroundingNodes.contains(piece.getLocation())) {
				return false;
			}
		}

		// Find the roads that are next to the tested location
		List<ReducedBoardPiece> adjRoads = new ArrayList<ReducedBoardPiece>();
		for (ReducedBoardPiece road : roads) {
			if (surroundingEdges.contains(road.getLocation())) {
				adjRoads.add(road);
			}
		}
		
		if(adjRoads.isEmpty()){
			return false;
		}

	
		// Check all the adjacent roads. One must be ours. The others must
		// either be blank or belong to two separate players
		boolean playerAdjacent = false;
		boolean opponentAdjacent = false;
		
		for (ReducedBoardPiece road : adjRoads) {
			int roadOwner =  road.getOwner();
			
			if(owner == roadOwner){
				playerAdjacent = true;
			}else{
				if(opponentAdjacent == true){
					return false; //its blocked
				}
				opponentAdjacent = true;
			}
		}
		
		return playerAdjacent;

	}

	public boolean isValidCity(int location, int owner) {
		return false;
	}

	public boolean addRoad(int location, int owner) {
		if (isValidRoad(location, owner)) {
			roads.add(new ReducedRoad(location, owner));
			return true;
		}
		return false;
	}

	public boolean addCity(int location, int owner) {
		if (isValidCity(location, owner)) {
			cities.add(new ReducedCity(location, owner));
			return true;
		}
		return false;
	}

	public boolean addSettlement(int location, int owner) {
		if (isValidSettlement(location, owner)) {
			settlements.add(new ReducedSettlement(location, owner));
			return true;
		}
		return false;
	}

	public List<ReducedBoardPiece> getRoads() {
		return roads;
	}

	public List<ReducedBoardPiece> getCities() {
		return cities;
	}

	public List<ReducedBoardPiece> getSettlements() {
		return settlements;

	}

	public SOCBoard getReferenceBoard() {
		return referenceBoard;
	}

}
