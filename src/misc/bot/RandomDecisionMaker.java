package misc.bot;

import java.util.ArrayList;

import misc.bot.moves.BotMove;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

public class RandomDecisionMaker extends DecisionMaker{

	public RandomDecisionMaker(SOCGame game, SOCPlayer ourPlayer) {
		super(game, ourPlayer);
	}

	@Override
	public ArrayList<BotMove> getMoveDecision() {
		return null;
	}

}
