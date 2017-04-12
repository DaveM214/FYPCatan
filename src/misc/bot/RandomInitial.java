package misc.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

public class RandomInitial extends InitialMoveDecider {

	public RandomInitial(SOCPlayer ourPlayer) {
		super(ourPlayer);
		// TODO Auto-generated constructor stub
	}

	public int handleDecision(int state, SOCBoard board){
		int result = 0;
		this.board = board;
		// If we need to build a settlement then build it
		if (state == SOCGame.START1A || state == SOCGame.START2A) {
			result = handleSettlementBuild();
			lastSettlementLocation = result;
			// If this is the first settlement we are placing then it decides on
			// the strategy we are playing
			/*
			if (state == SOCGame.START1A) {
				decideStrategy(result);
			}
			*/

			// Otherwise we are building a road
		} else {
			result = handleRoadBuild();
		}

		return result;
	}
	
	private int handleSettlementBuild(){
		ArrayList<Integer> locs = generatePossibleSetLocs();
		Random rand = new Random();
		return locs.get(rand.nextInt(locs.size()));
		
	}
	
	private int handleRoadBuild(){
		List<Integer> locs = generatePossibleRoadLocs();
		Random rand = new Random();
		return locs.get(rand.nextInt(locs.size()));
	}
	
}
