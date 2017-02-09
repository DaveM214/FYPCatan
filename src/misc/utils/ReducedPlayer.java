package misc.utils;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

/**
 * Class representing a simplified player
 * 
 * @author david
 *
 */
public class ReducedPlayer {

	private int playerNumber;
	private int victoryPoints;
	private int[] resources;
	
	/**
	 * Constructor. Create a reduced player.
	 * 
	 * @param playerNumber This players id number (0-3)
	 * @param victoryPoints The number of victory points the player has.
	 */
	public ReducedPlayer(int playerNumber, int victoryPoints){
		this.playerNumber = playerNumber;
		this.victoryPoints = victoryPoints;
		resources = new int[5]; //Using standard constants
	}
	
	/**
	 * Copy constructor. Create new object from an original
	 * @param orig
	 */
	public ReducedPlayer(ReducedPlayer orig){
		this.playerNumber = orig.getPlayerNumber();
		this.victoryPoints = orig.getVictoryPoints();
		int[] temp = new int[5];
		//TODO sort out copying player.
	}
	/**
	 * Retrieve the player number
	 * @return The ID of the player
	 */
	public int getPlayerNumber() {
		return playerNumber;
	}

	/**
	 * Get the amount of victory points the player has.
	 * @return
	 */
	public int getVictoryPoints() {
		return victoryPoints;
	}
	
	/**
	 * Set the amount of victory points the player has.
	 * @param vp
	 */
	public void setVictoryPoints(int vp){
		this.victoryPoints = vp;
	}
	
	/**
	 * Get the list of resources that a player has.
	 * @return
	 */
	public int[] getResources(){
		return this.resources;
	}
	
}
