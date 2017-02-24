package misc.utils;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import soc.game.SOCInventory;
import soc.game.SOCPlayer;
import soc.game.SOCPlayingPiece;
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;

/**
 * Class representing a simplified player
 * 
 * @author david
 *
 */
public class ReducedPlayer {

	private int playerNumber;
	private int victoryPoints;
	private int[] developmentCards;
	private int[] resources;
	private boolean[] bankTradedRec;
	private boolean devCardPlayed;
	private int settlementPieces;
	private int cityPieces;
	private int roadPieces;

	/**
	 * Constructor. Create a reduced player.
	 * 
	 * @param playerNumber
	 *            This players id number (0-3)
	 * @param victoryPoints
	 *            The number of victory points the player has.
	 */
	public ReducedPlayer(int playerNumber, int victoryPoints) {
		this.playerNumber = playerNumber;
		this.victoryPoints = victoryPoints;
		resources = new int[5]; // Using standard constants
		bankTradedRec = new boolean[] { false, false, false, false, false };
	}

	public ReducedPlayer(SOCPlayer player) {
		this.playerNumber = player.getPlayerNumber();
		this.victoryPoints = player.getTotalVP();
		resources = new int[5];
		SOCResourceSet resourceSet = player.getResources();

		resources[0] = resourceSet.getAmount(SOCResourceConstants.CLAY);
		resources[1] = resourceSet.getAmount(SOCResourceConstants.ORE);
		resources[2] = resourceSet.getAmount(SOCResourceConstants.SHEEP);
		resources[3] = resourceSet.getAmount(SOCResourceConstants.WHEAT);
		resources[4] = resourceSet.getAmount(SOCResourceConstants.WOOD);

		settlementPieces = player.getNumPieces(SOCPlayingPiece.SETTLEMENT);
		cityPieces = player.getNumPieces(SOCPlayingPiece.CITY);
		roadPieces = player.getNumPieces(SOCPlayingPiece.ROAD);
		bankTradedRec = new boolean[] { false, false, false, false, false };
		
		developmentCards = new int[10];
		SOCInventory inv = player.getInventory();
		
		for(int i=0;i<developmentCards.length;i++){
			developmentCards[i] = inv.getAmount(i);
		}
		devCardPlayed = false;
	}
	

	/**
	 * Copy constructor. Create new object from an original
	 * 
	 * @param orig
	 */
	public ReducedPlayer(ReducedPlayer orig) {
		this.playerNumber = orig.getPlayerNumber();
		this.victoryPoints = orig.getVictoryPoints();

		resources = new int[5];
		int[] tempResources = orig.getResources();

		for (int i = 0; i < tempResources.length; i++) {
			resources[i] = tempResources[i];
		}
		
		
		int[] developmentCardsTemp = orig.getDevelopmentCards();
		developmentCards = new int[10];
		System.arraycopy(developmentCardsTemp, 0, developmentCards, 0, developmentCardsTemp.length);

		settlementPieces = orig.getSettlementPieces();
		cityPieces = orig.getCityPieces();
		roadPieces = orig.getRoadPieces();
		bankTradedRec = orig.copyResourceArray();
	}

	/**
	 * Helper method for copying resource array
	 * 
	 * @return
	 */
	private boolean[] copyResourceArray() {
		boolean[] copyArray = new boolean[5];
		for (int i = 0; i < bankTradedRec.length; i++) {
			copyArray[i] = bankTradedRec[i];
		}
		return copyArray;
	}

	/**
	 * Get the resources that the player has received from the bank
	 * 
	 * @return
	 */
	public boolean[] getTradedResourceArray() {
		return this.bankTradedRec;
	}
	
	/**
	 * Set the array of resource that we have traded.
	 * @param array
	 */
	public void setTradedResourceArray(boolean[] array){
		this.bankTradedRec = array;
	}
	
	/**
	 * Change as value indicating whether or not we have traded this resource from the bank.
	 * 
	 * @param resource The resource being set. 0-4
	 * @param value The boolean value it is changing to.
	 */
	public void setTradedResourceArray(int resource, boolean value){
		bankTradedRec[resource] = value;
	}

	/**
	 * Retrieve the player number
	 * 
	 * @return The ID of the player
	 */
	public int getPlayerNumber() {
		return playerNumber;
	}

	/**
	 * Get the amount of victory points the player has.
	 * 
	 * @return
	 */
	public int getVictoryPoints() {
		return victoryPoints;
	}

	/**
	 * Set the amount of victory points the player has.
	 * 
	 * @param vp
	 */
	public void setVictoryPoints(int vp) {
		this.victoryPoints = vp;
	}

	/**
	 * Get the list of resources that a player has.
	 * 
	 * @return
	 */
	public int[] getResources() {
		return this.resources;
	}
	
	public int getResource(int rNumber){
		return this.resources[rNumber];
	}

	/**
	 * Set the specified resource that the player possesses to be a certain
	 * value
	 * 
	 * @param rNumber
	 *            0-4 the resource being changed. NB this is one less that the
	 *            values in {@link SOCResourceConstants}
	 * @param amount
	 *            The new value
	 */
	public void setResource(int rNumber, int amount) {
		this.resources[rNumber] = amount;
	}

	/**
	 * Add one to the value of a resource.
	 */
	public void incrementResource(int rNumber) {
		this.resources[rNumber]++;
	}

	/**
	 * Take one from the value of the resource. The resource number is our
	 * representation 0-4
	 */
	public void decrementResource(int rNumber) {
		this.resources[rNumber]--;
	}

	public int getSettlementPieces() {
		return settlementPieces;
	}

	public int getCityPieces() {
		return cityPieces;
	}

	public int getRoadPieces() {
		return roadPieces;
	}

	public void decrementRoadPieces() {
		roadPieces--;
	}

	public void decrementSettlementPieces() {
		settlementPieces--;
	}

	public void decrementCityPieces() {
		cityPieces--;
	}

	/**
	 * Settlements are the only ones we need to be able to increment. Everything
	 * else when placed is final. We get settlements back when we build cities.
	 */
	public void incrementSettlementPieces() {
		settlementPieces++;
	}

	/**
	 * Return true if this player has a 3:1 port. Otherwise return false.
	 * 
	 * @return Whether the player has a 3:1 trade port or not.
	 */
	public boolean hasGeneralPort() {
		return false;
	}
	
	public void setDevCardPlayed(boolean played){
		this.devCardPlayed = played;
	}
	
	public boolean getDevCardPlayed(){
		return devCardPlayed;
	}

	/**
	 * Return boolean array of whether we have specialist 2:1 trade ports for a
	 * specific resource.
	 * 
	 * @return A true false list for each resource. True if we have a 2:1 trade
	 *         port it and false if we do not.
	 */
	public boolean[] hasSpecPorts() {
		return new boolean[] { false, false, false, false, false };
	}
	
	public int[] getDevelopmentCards(){
		return this.developmentCards;
	}

	public void decrementDevelopmentCards(int i) {
		developmentCards[i]--;
	}

}
