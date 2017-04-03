package misc.bot.decisionMakers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import misc.bot.moves.BotMove;
import misc.bot.moves.PiecePlacement;
import misc.bot.moves.Trade;
import misc.simulator.Simulator;
import misc.utils.GameUtils;
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
	private static final int NORMAL_ROAD_SCORE = -7;
	private static final int PRIORITY_ROAD_SCORE = 10;
	private static final int BANK_TRADE_SCORE = -2;
	private static final int SETTLEMENT_SCORE = 30;
	private static final int BUY_DEV_CARD = 5;
	private static final int DO_NOTHING_SCORE = -6;

	private int hexWeAreRobbing = 0;

	public SimpleHeuristicDecisionMaker(SOCGame game) {
		super(game);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<BotMove> getMoveDecision() {

		ArrayList<ArrayList<BotMove>> possMoves = getAllPossibleMoves();
		int bestScore = Integer.MIN_VALUE;
		List<List<BotMove>> bestMoves = new ArrayList<>();
		for (ArrayList<BotMove> moveList : possMoves) {
			int score = scoreMoves(moveList);
			if (score > bestScore) {
				bestMoves.clear();
				bestMoves.add(moveList);
				bestScore = score;
			} else if (score == bestScore) {
				bestMoves.add(moveList);
			}
		}

		// If there is only one move return it
		if (bestMoves.size() == 1) {
			return bestMoves.get(0);
		} else {
			return tieBreakBestMoves(bestMoves);
		}
	}

	/**
	 * Decide which move out of a set that ties is better. We will randomly tie
	 * for now to introduce some variety. A better idea may be to explore which
	 * of the moves will put us into a better position in terms of available
	 * settlement locations that we could build
	 * 
	 * @param bestMoves
	 * @return The list of moves that has been chosen.
	 */
	private List<BotMove> tieBreakBestMoves(List<List<BotMove>> bestMoves) {
		return bestMoves.get(new Random().nextInt(bestMoves.size()));
	}

	private int scoreMoves(ArrayList<BotMove> moveList) {
		int score = 0;

		boolean needsRoads = false;
		if ((reducedGame.getBoard().getLegalSettlementLocations(ourPlayer.getPlayerNumber()).size()) == 0) {
			needsRoads = true;
		}

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
						if (!needsRoads) {
							score += NORMAL_ROAD_SCORE;
						} else {
							score += PRIORITY_ROAD_SCORE;
						}
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
		Map<Integer,Integer> robberLocationScores = scoreRobberLocations(robberLocations);
		
		int bestScore = -1;
		int bestLocation = -1;
		
		for (Integer location : robberLocationScores.keySet()) {
			int score = robberLocationScores.get(location);
			if(score > bestScore){
				bestScore = score;
				bestLocation = location;
			}
		}
		
		hexWeAreRobbing = bestLocation;
		return bestLocation;
	}

	/**
	 * We will score the robber locations based on how much resource they will
	 * deprive our opponents of for this simple decision maker.
	 * 
	 * @param robberLocations
	 * @return
	 */
	private Map<Integer,Integer> scoreRobberLocations(List<Integer> robberLocations) {
		Map<Integer,Integer> scoreMap = new HashMap<Integer,Integer>();
		for (Integer location : robberLocations) {
			int score = 0;
			List<ReducedBoardPiece> surrounding = reducedGame.getBoard().getSettlementsAroundHex(location);
			for (ReducedBoardPiece reducedBoardPiece : surrounding) {
				if(reducedBoardPiece.getType() == SOCPlayingPiece.CITY){
					score+=2;
				}else{
					score++;
				}
			}
			scoreMap.put(location, score);
		}
		return scoreMap;
	}
	
	
	
	@Override
	public int[] getRobberDiscard(int discard) {
		return getDiscardResources(discard);
	}

	private int[] getDiscardResources(int resourcesToDiscard) {
		int[] ourResources = new int[5];
		System.arraycopy(getOurResources(), 0, ourResources, 0, ourResources.length);
		int[] discardSet = new int[] { 0, 0, 0, 0, 0 };
		int resourcesDiscarded = 0;
		int index = 0;

		while (resourcesDiscarded < resourcesToDiscard) {
			if (ourResources[index] > 0) {
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
		return discardSet;
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
