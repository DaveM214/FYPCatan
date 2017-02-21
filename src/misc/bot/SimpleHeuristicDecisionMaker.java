package misc.bot;

import java.util.ArrayList;
import java.util.List;

import misc.bot.moves.BotMove;
import misc.bot.moves.PiecePlacement;
import misc.bot.moves.Trade;
import soc.game.SOCGame;
import soc.game.SOCPlayingPiece;

/**
 * A basic decision maker that uses some light heuristics to make better
 * decisions than random moves.
 * 
 * Examples of these heuristics are:- Building roads only if there are no
 * settlements available for us to build. Building settlements and cities
 * whenever possible
 * 
 * @author david
 *
 */
public class SimpleHeuristicDecisionMaker extends DecisionMaker {

	// MAGIC NUMBERS!
	private static final int CITY_SCORE = 40;
	private static final int ROAD_SCORE = 0;
	private static final int BANK_TRADE_SCORE = -2;
	private static final int SETTLEMENT_SCORE = 30;
	private static final int BUY_DEV_CARD = 12;
	private static final int DO_NOTHING_SCORE = -10;

	public SimpleHeuristicDecisionMaker(SOCGame game) {
		super(game);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<BotMove> getMoveDecision() {
		ArrayList<ArrayList<BotMove>> possMoves = getAllPossibleMoves();
		System.out.println("Possible Moves: " + possMoves.size());
		int bestScore = Integer.MIN_VALUE;
		int bestIndex = -1;
		int i = 0;
		for (ArrayList<BotMove> moveList : possMoves) {
			int score = scoreMoves(moveList);
			if (score > bestScore) {
				bestScore = score;
				bestIndex = i;
			}
			i++;
		}

		return possMoves.get(bestIndex);
	}

	private int scoreMoves(ArrayList<BotMove> moveList) {
		int score = 0;

		if (moveList.isEmpty()) {
			score = DO_NOTHING_SCORE;
		} else {

			for (BotMove botMove : moveList) {
				switch (botMove.getMoveType()) {
				case BotMove.PIECE_PLACEMENT:
					PiecePlacement placement = (PiecePlacement) botMove;

					if (placement.getPieceType() == SOCPlayingPiece.SETTLEMENT) {
						score += SETTLEMENT_SCORE;
					} else if (placement.getPieceType() == SOCPlayingPiece.CITY) {
						score += SETTLEMENT_SCORE;
					} else {
						score += ROAD_SCORE;
					}
					break;

				case BotMove.DEV_CARD_BUY:
					score += BUY_DEV_CARD;
					break;

				case BotMove.TRADE:
					Trade trade = (Trade) botMove;
					if (trade.getTradeTarget() == -1) {
						score += BANK_TRADE_SCORE;
					}
					break;

				default:
					break;
				}
			}
		}

		return score;
	}

}
