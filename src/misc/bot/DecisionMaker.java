package misc.bot;

import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

public abstract class DecisionMaker {
	
	private SOCGame game;
	private SOCPlayer ourPlayer;

	public DecisionMaker(SOCGame game, SOCPlayer ourPlayer) {
		this.game = game;
		this.ourPlayer = ourPlayer;
	}
	
	/**
	 * Update the game state that we are making our decision based on.
	 */
	public void updateGame(SOCGame updatedGame){
		game = updatedGame;
	}
	
	public void setOurPlayerInformation(SOCPlayer ourPlauer){
		this.ourPlayer = ourPlayer;
	}
	
	public abstract void getMoveDecision();
	
	
}
