package misc.utils;

import java.util.ArrayList;
import java.util.List;

import soc.game.SOCBoard;

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
	 * Constructor that creates a reduced board from a regular SOCBoard.
	 * Copy roads etc. from the previous board.
	 * 
	 * @param board
	 */
	public ReducedBoard(SOCBoard board) {
		roads = new ArrayList<>();
		settlements = new ArrayList<>();
		cities = new ArrayList<>();
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
	
	//TODO wednesday -- finish off checking for illegal moves and sort out the BuildNode Class that sorts the searching for possible building moves combinations.
	
	public List<Integer> getLegalSettlementLocations(int player){
		return null;
	}
	
	public List<Integer> getLegalRoadLocations(int player){
		return null;
	}
	
	public List<Integer> getLegalCityLocations(int player){
		return null;
	}
	
	public boolean isValidRoad(int location, int owner){
		return false;
	}
	
	public boolean isValidSettlement(int location, int owner){
		return false;
	}
	
	public boolean isValidCity(int location, int owner){
		return false;
	}
	
	public boolean addRoad(int location, int owner){
		return true;
	}
	
	public boolean addCity(int location, int owner){
		return true;
	}
	
	public boolean addSettlement(int location, int owner){
		return true;
	}
	
	public List<ReducedRoad> getRoads(){
		return roads;
	}
	
	public List<ReducedCity> getCities(){
		return cities;
	}
	
	public List<ReducedSettlement> getSettlements(){
		return settlements;
		
	}
	
	
}
