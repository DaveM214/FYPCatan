package misc.bot;

import java.util.ArrayList;
import java.util.Random;

import misc.bot.moves.BotMove;
import soc.game.SOCGame;
import soc.game.SOCPlayer;

public class RandomDecisionMaker extends DecisionMaker{

	public RandomDecisionMaker(SOCGame game) {
		super(game);
	}

	@Override
	public ArrayList<BotMove> getMoveDecision() {
		ArrayList<ArrayList<BotMove>> possMoves = getAllPossibleMoves();
		System.out.println("Possible Moves: " + possMoves.size());
		Random rand = new Random();
		return possMoves.get(rand.nextInt(possMoves.size()));
	}

	@Override
	public int getNewRobberLocation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getRobberDiscard() {
		// TODO Auto-generated method stub
		return null;
	}

}
