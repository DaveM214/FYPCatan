package misc.bot.decisionMakers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import misc.bot.moves.BotMove;
import misc.bot.moves.PiecePlacement;
import misc.bot.moves.Trade;
import misc.simulator.Simulator;
import misc.utils.ReducedBoardPiece;
import misc.utils.ReducedGame;
import misc.utils.exceptions.SimNotInitialisedException;
import soc.game.SOCGame;
import soc.game.SOCPlayingPiece;
import soc.game.SOCResourceSet;

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

	private int hexWeAreRobbing = 0;

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
						score += CITY_SCORE;
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

	@Override
	public int getNewRobberLocation() {
		List<Integer> robberLocations = getPossibleRobberLocations();
		List<Integer> robberLocationScores = scoreRobberLocations(robberLocations);
		int ourRobberLocation = robberLocations.get((getBestIndex(robberLocationScores)));
		hexWeAreRobbing = ourRobberLocation;
		return ourRobberLocation;
	}

	private int getBestIndex(List<Integer> robberLocationScores) {
		int bestIndex = -1;
		int bestScore = Integer.MIN_VALUE;
		for (int i = 0; i < robberLocationScores.size(); i++) {
			if (robberLocationScores.get(i) > bestScore) {
				bestIndex = i;
				bestScore = robberLocationScores.get(i);
			}
		}
		return bestIndex;
	}

	/**
	 * We will score the robber locations based on how much resource they will
	 * deprive our opponents of for this simple decision maker.
	 * 
	 * @param robberLocations
	 * @return
	 */
	private List<Integer> scoreRobberLocations(List<Integer> robberLocations) {
		List<Integer> robberLocationScores = new ArrayList<Integer>();
		for (Integer location : robberLocations) {
			int hexScore = 0;
			int numberOnHex = game.getBoard().getNumberOnHexFromCoord(location);
			int baseScore = Math.abs(7 - numberOnHex);
			List<ReducedBoardPiece> surroundingSettlements = reducedGame.getBoard().getSettlementsAroundHex(location);
			for (ReducedBoardPiece reducedBoardPiece : surroundingSettlements) {
				if (reducedBoardPiece.getType() == SOCPlayingPiece.SETTLEMENT) {
					hexScore += baseScore;
				} else {
					hexScore += baseScore * 2;
				}
			}
			robberLocationScores.add(hexScore);
		}
		return robberLocationScores;
	}

	@Override
	public int[] getRobberDiscard() {
		int[] resources = getOurResources();
		int[] discardArray = new int[] { 0, 0, 0, 0, 0 };
		int totalResources = 0;
		for (int i : resources) {
			totalResources += i;
		}

		if (totalResources > 7) {
			int resourcesToDiscard = totalResources / 2;
			discardArray = getDiscardResources(resourcesToDiscard);
		}

		return discardArray;
	}

	private int[] getDiscardResources(int resourcesToDiscard) {
		int[] ourResources = new int[5];
		System.arraycopy(getOurResources(), 0, ourResources, 0, ourResources.length);
		int[] discardSet = new int[] { 0, 0, 0, 0, 0 };
		int resourcesDiscarded = 0;
		int index = 0;

		while (resourcesDiscarded < resourcesDiscarded) {
			if (ourResources[index] > 1) {
				discardSet[index]++;
				ourResources[index]--;
				resourcesDiscarded++;
			}
			if (index == 4) {
				index = 0;
			} else {
				index++;
			}
		}

		return ourResources;
	}

	@Override
	/**
	 * The player we are going to take the resource from.
	 */
	public int getRobberTarget() {
		List<ReducedBoardPiece> possTargets = reducedGame.getBoard().getSettlementsAroundHex(hexWeAreRobbing);

		// Steal from a random target
		Random rand = new Random();
		return possTargets.get(rand.nextInt(possTargets.size())).getOwner();
	}

}
