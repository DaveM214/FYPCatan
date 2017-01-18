package misc.utils;

import java.util.ArrayList;
import java.util.Vector;

import soc.game.SOCBoard;

public class SettlementResourceInfo {

	private final int location;
	private ArrayList<Integer> values;
	private ArrayList<Integer> resources;
	private SOCBoard board;

	public SettlementResourceInfo(int location, SOCBoard board) {
		this.location = location;
		this.values = new ArrayList<Integer>();
		this.resources = new ArrayList<Integer>();
		calculateInformation();
	}

	public ArrayList<Integer> getValues() {
		return values;
	}

	public ArrayList<Integer> getResources() {
		return resources;
	}

	public int getLocation() {
		return location;
	}

	/**
	 * Helper method that calculates what hexes are next to the possible
	 * location and the values that are assigned to those hexes
	 */
	private void calculateInformation() {
		//Get all the hexes that neighbour that node
		Vector<Integer>hexes =  board.getAdjacentHexesToNode(location);
		
		//Add their resource and dice number to the list
		for (Integer hex : hexes) {
			int type = board.getHexTypeFromCoord(hex);
			
			//We only care about it if it is a resource gathering hex
			if(type >= SOCBoard.CLAY_HEX && type <= SOCBoard.WOOD_HEX){
				//Add the type and the value
				resources.add(type);
				values.add(board.getNumberOnHexFromCoord(hex));
			}
				
		}
	}

}
