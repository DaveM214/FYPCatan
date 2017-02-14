package misc.utils;

import java.util.ArrayList;
import java.util.List;

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

	private List<ReducedRoad> roads;
	private List<ReducedSettlement> settlements;
	private List<ReducedCity> cities;

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
		roads = new ArrayList<ReducedRoad>();
		settlements = new ArrayList<ReducedSettlement>();
		cities = new ArrayList<ReducedCity>();

		for (ReducedCity city : orig.getCities()) {
			ReducedCity newCity = new ReducedCity(city);
			cities.add(newCity);
		}

		for (ReducedRoad road : orig.getRoads()) {
			ReducedRoad newRoad = new ReducedRoad(road);
			roads.add(newRoad);
		}

		for (ReducedSettlement settlement : orig.getSettlements()) {
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
		return null;
	}

	public List<Integer> getLegalRoadLocations(int player) {
		return null;
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
		for (ReducedSettlement settlement : settlements) {
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
		return false;
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

	public List<ReducedRoad> getRoads() {
		return roads;
	}

	public List<ReducedCity> getCities() {
		return cities;
	}

	public List<ReducedSettlement> getSettlements() {
		return settlements;

	}

}
