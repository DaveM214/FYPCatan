package misc.bot;

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
	
	
	
}
