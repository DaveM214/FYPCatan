package misc.bot.decisionMakers;

import java.util.ArrayList;

import misc.bot.moves.BotMove;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

public class MixedDecisionMaker extends DecisionMaker{

	public MixedDecisionMaker(SOCGame game){
		super(game);
	}

	@Override
	public ArrayList<BotMove> getMoveDecision() {
		return null;
		
	}

	@Override
	public int getNewRobberLocation() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getRobberTarget() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getRobberDiscard(int discards) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
