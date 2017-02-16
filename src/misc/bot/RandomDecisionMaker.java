package misc.bot;

import java.util.ArrayList;
import java.util.Random;

import misc.bot.moves.BotMove;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

public class RandomDecisionMaker extends DecisionMaker{

	public RandomDecisionMaker(SOCGame game, SOCPlayer ourPlayer) {
		super(game, ourPlayer);
	}

	@Override
	public ArrayList<BotMove> getMoveDecision() {
		System.out.println("Getting all possible moves");
		ArrayList<ArrayList<BotMove>> possMoves = getAllPossibleMoves();
		System.out.println("All possible moves received");
		
		Random rand = new Random();
		return possMoves.get(rand.nextInt(possMoves.size()));
	}

}
