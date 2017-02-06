package misc.utils;

/**
 * Class representing a simplified player
 * 
 * @author david
 *
 */
public class ReducedPlayer {

	private int playerNumber;
	private int victoryPoints;
	
	/**
	 * Constructor. Create a reduced player.
	 * 
	 * @param playerNumber This players id number (0-3)
	 * @param victoryPoints The number of victory points the player has.
	 */
	public ReducedPlayer(int playerNumber, int victoryPoints){
		this.playerNumber = playerNumber;
		this.victoryPoints = victoryPoints;
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
	
}
