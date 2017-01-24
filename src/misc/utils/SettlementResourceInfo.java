package misc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import soc.game.SOCBoard;

public class SettlementResourceInfo {

	private final int location;
	private ArrayList<Integer> values;
	private ArrayList<Integer> resources;
	private double avgResourceWeight;
	private SOCBoard board;

	// We will multiply the average weight if it has access to less than 3
	// cities.
	private static final double MULTIPLIER_1 = 5;
	private static final double MULTIPLIER_2 = 2.5;

	public SettlementResourceInfo(int location, SOCBoard board) {
		this.location = location;
		this.values = new ArrayList<Integer>();
		this.resources = new ArrayList<Integer>();
		this.board = board;
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
	 * 
	 * @return
	 */
	public double getAvgResourceWeight() {
		return avgResourceWeight;
	}

	/**
	 * Return the average resource weight adjusted for the possibility of it not
	 * having 3 possible settlements. However you cannot discount them
	 * completely because there are cases where you may want to use them. We will triple 
	 * 
	 * @return
	 */
	public double getAdjustedResourceWeight() {
		if (values.size() == 2) {
			return avgResourceWeight * MULTIPLIER_2;
		} else if (values.size() == 1) {
			return avgResourceWeight * MULTIPLIER_1;
		} else {
			return avgResourceWeight;
		}
	}

	/**
	 * Helper method that calculates what hexes are next to the possible
	 * location and the values that are assigned to those hexes
	 */
	private void calculateInformation() {
		// Get all the hexes that neighbour that node
		Vector<Integer> hexes = board.getAdjacentHexesToNode(location);
		double totalDiceValue = 0;

		// Add their resource and dice number to the list
		for (Integer hex : hexes) {

			int type = board.getHexTypeFromCoord(hex);

			// We only care about it if it is a resource gathering hex
			if (type >= SOCBoard.CLAY_HEX && type <= SOCBoard.WOOD_HEX) {
				// Add the type and the value
				resources.add(type);
				values.add(board.getNumberOnHexFromCoord(hex));
			}

			int value = board.getNumberOnHexFromCoord(hex);
			totalDiceValue += Math.abs(7 - value);
		}

		avgResourceWeight = totalDiceValue / hexes.size();
	}

}
