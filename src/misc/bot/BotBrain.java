package misc.bot;

import misc.utils.BotMessageQueue;
import misc.utils.exceptions.SimNotInitialisedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import misc.bot.BotClient;
import misc.bot.decisionMakers.DecisionMaker;
import misc.bot.decisionMakers.RandomDecisionMaker;
import misc.bot.decisionMakers.SimpleHeuristicDecisionMaker;
import misc.bot.moves.BotMove;
import misc.bot.moves.PiecePlacement;
import misc.bot.moves.PlayDevCard;
import misc.bot.moves.PlayMonopoly;
import misc.bot.moves.PlayRoadBuilding;
import misc.bot.moves.PlayYOP;
import misc.bot.moves.Trade;
import soc.baseclient.SOCDisplaylessPlayerClient;
import soc.game.SOCBoard;
import soc.game.SOCCity;
import soc.game.SOCDevCardConstants;
import soc.game.SOCGame;
import soc.game.SOCInventory;
import soc.game.SOCPlayer;
import soc.game.SOCPlayingPiece;
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;
import soc.game.SOCRoad;
import soc.game.SOCSettlement;
import soc.message.SOCDevCardAction;
import soc.message.SOCFirstPlayer;
import soc.message.SOCGameState;
import soc.message.SOCMessage;
import soc.message.SOCPlayerElement;
import soc.message.SOCPutPiece;
import soc.message.SOCResourceCount;
import soc.message.SOCSetTurn;
import soc.message.SOCTurn;

/**
 * Class to direct the AI of the bot. This class will maintain a state of the
 * current game and server as the point of communication to the client which
 * will handle the communication with the server. This class acts as the
 * interface between the decision making and the client. This is the only class
 * that is allowed to call the client to send messages.
 * 
 * @author david
 *
 */
public class BotBrain extends Thread {

	private BotMessageQueue<SOCMessage> msgQ;
	private DecisionMaker dm;
	private BotClient client;
	private SOCBoard board;
	private SOCGame game;
	private SOCPlayer ourPlayer;
	private boolean boardInit = false;
	private int seatNumber; // Our seatNumber
	private boolean alive;
	private int currentPlayer;
	private int firstPlayer;
	private int ourPriority; // The order of the first move we will make
	private int turnCounter; // Number of turns there have been
	private int ourTurnCounter;// Number of turns we have had
	private boolean ourTurn;
	// private int desiredBuildLocation;
	// private int desiredBuildType;

	// Possible playing strategy constants
	public final static int BRICK_STRATEGY = 1;
	public final static int ORE_STRATEGY = 2;
	public final static int MIXED_STRATEGY = 3;

	private InitialMoveDecider initialDecider;
	private boolean expectingMove = false;
	private boolean waitingForGameState = false;
	private boolean expectingDiceRoll = false;
	private List<BotMove> movesToProcess;
	private boolean expectingPiecePlacement;
	private List<PiecePlacement> buildList;
	private boolean expectingDevCard;

	public BotBrain(BotClient client, SOCGame game, BotMessageQueue<SOCMessage> msgQueue) {
		msgQ = msgQueue;
		this.client = client;
		this.game = game;
		alive = true;
		expectingDevCard = false;
		dm = new RandomDecisionMaker(game);
		movesToProcess = new ArrayList<BotMove>();
		buildList = new ArrayList<PiecePlacement>();
	}

	/**
	 * TODO complete this method. It should elegantly kill the bot.
	 */
	public void kill() {

	}

	/**
	 * Thread run method. Starts the execution of the brain and handles the
	 * messages that are reveived from the server.
	 */
	@Override
	public void run() {
		init();
		while (alive) {
			try {
				// This method sleeps until there is an element in the queue
				SOCMessage msg = msgQ.get();
				int msgType = msg.getType();
				System.out.println(msg.toString());

				switch (msgType) {

				// We have received a new game state from the server.
				case SOCMessage.GAMESTATE:
					updateGameState(((SOCGameState) msg).getState());
					break;

				// Handle the message that indicates who the first player to
				// move is.
				case SOCMessage.FIRSTPLAYER:
					setFirstPlayer(((SOCFirstPlayer) msg).getPlayerNumber());
					break;

				// Handle the message that is indicates its is someone elses
				// turn
				case SOCMessage.SETTURN:
					setTurnNumber((((SOCSetTurn) msg).getPlayerNumber()));
					break;

				// Handle the message that indicates someones turn is starting
				case SOCMessage.TURN:
					handleTurnMessage(((SOCTurn) msg));
					break;

				case SOCMessage.PLAYERELEMENT:
					handlePlayerElement(((SOCPlayerElement) msg));
					break;

				// Handle the message showing how many resources a player has.
				case SOCMessage.RESOURCECOUNT:
					handleResourceCount(msg);
					break;

				// Handle the message that describes a player placing a piece on
				// the board.
				case SOCMessage.PUTPIECE:
					handlePutPiece((SOCPutPiece) msg);
					break;

				case SOCMessage.BUILDREQUEST:
					if (ourTurn) {
						handleBuildRequest();
					}
					break;

				case SOCMessage.DEVCARDACTION:
					handleDEVCARDACTION((SOCDevCardAction) msg);
					break;

				default:
					break;
				}

				// If it is our turn then enact our turn
				if (ourTurn) {
					if (expectingMove & !waitingForGameState) {
						handleOurTurn();
					}
				}

				yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void handleBuildRequest() {
		// TODO Auto-generated method stub

	}

	/**
	 * Handle a DEVCARDACTION for this game. No brain-specific action.
	 * 
	 * @since 1.1.08
	 */
	private void handleDEVCARDACTION(SOCDevCardAction mes) {
		if (mes.getPlayerNumber()!= -1) {
			SOCInventory cardsInv = game.getPlayer(mes.getPlayerNumber()).getInventory();
			final int cardType = mes.getCardType();

			switch (mes.getAction()) {
			case SOCDevCardAction.DRAW:
				cardsInv.addDevCard(1, SOCInventory.NEW, cardType);
				break;

			case SOCDevCardAction.PLAY:
				cardsInv.removeDevCard(SOCInventory.OLD, cardType);
				break;

			case SOCDevCardAction.ADDOLD:
				cardsInv.addDevCard(1, SOCInventory.OLD, cardType);
				break;

			case SOCDevCardAction.ADDNEW:
				cardsInv.addDevCard(1, SOCInventory.NEW, cardType);
				break;
			}
		}
	}

	/**
	 * Method to handle someone placing a piece on the game board. Updates the
	 * game board.
	 * 
	 * @param msg
	 *            The message describing who placed what piece and where.
	 */
	private void handlePutPiece(SOCPutPiece msg) {
		System.out.println("Placing piece");
		SOCPlayer player = game.getPlayer(msg.getPlayerNumber());

		SOCPlayingPiece piece = null;
		if (msg.getPieceType() == SOCPlayingPiece.SETTLEMENT) {
			piece = new SOCSettlement(player, msg.getCoordinates(), game.getBoard());
		} else if (msg.getPieceType() == SOCPlayingPiece.ROAD) {
			piece = new SOCRoad(player, msg.getCoordinates(), game.getBoard());
		} else if (msg.getPieceType() == SOCPlayingPiece.CITY) {
			piece = new SOCCity(player, msg.getCoordinates(), game.getBoard());
		}

		if (piece != null) {
			player.putPiece(piece, false);
			game.putPiece(piece);
		}
	}

	/**
	 * Helper method that handles how our turn should be run. This method mainly
	 * branches between handling the initial moves, which follow a different set
	 * of rules to the rest of the game, and the regular game moves.
	 */
	private void handleOurTurn() {
		int currentState = game.getGameState();
		System.out.println("Current State on our turn: " + currentState);

		// IF Initial state
		if (currentState >= 5 && currentState <= 11) {
			// We will do initial placement manually through probabilities and
			// stats
			doInitialPlacement(currentState);
			expectingDiceRoll = true;

		}
		// Otherwise we are in the main game.
		if (currentState == SOCGame.PLAY && expectingDiceRoll) {
			System.out.println("ROLLING DICE!");
			requestDiceRoll();
			expectingDiceRoll = false;
		}

		if (currentState == SOCGame.PLACING_CITY || currentState == SOCGame.PLACING_ROAD
				|| currentState == SOCGame.PLACING_SETTLEMENT) {
			expectingPiecePlacement = false;
			handlePiecePlacement(buildList.get(0));
			buildList.remove(0);
			if (buildList.isEmpty()) {
				expectingDiceRoll = true;
				waitingForGameState = true;
				expectingMove = false;
				client.endTurn(game);
				System.out.println("");
			} else {
				requestPiecePlacement(buildList.get(0));
			}

		}

		if (currentState == SOCGame.PLAY1 && !expectingPiecePlacement) {

			System.out.println("\nHandling our turn");
			handleMainTurn();
			expectingDiceRoll = true;
		}

	}

	/**
	 * Method to handle the playing of the regular turns in the game. This
	 * involves moving the robber or playing a knight card (TODO) if required
	 * and then rolling the dice and playing our own moves.
	 * 
	 */
	private void handleMainTurn() {
		dm.updateGame(game);
		dm.setOurPlayerInformation(ourPlayer);

		movesToProcess = dm.getMoveDecision();

		System.out.println(ourPlayer.getResources().toFriendlyString());
		System.out.println(game.getPlayer(ourPlayer.getPlayerNumber()).getResources().toFriendlyString());
		System.out.println("Playing Following Moves:" + movesToProcess.toString());

		List<BotMove> devCard = new ArrayList<>();
		List<BotMove> trades = new ArrayList<>();
		List<PiecePlacement> builds = new ArrayList<>();
		List<BotMove> buys = new ArrayList<>();

		for (BotMove botMove : movesToProcess) {

			if (botMove.getMoveType() == 2) {
				trades.add(botMove);
			}
			if (botMove.getMoveType() == 1) {
				builds.add((PiecePlacement) botMove);
			}
			if (botMove.getMoveType() == 3) {
				buys.add(botMove);
			}
			if (botMove.getMoveType() == 4) {
				devCard.add(botMove);
			}
		}

		// Handle dev card playing first if necessary
		if (!devCard.isEmpty()) {
			PlayDevCard move = (PlayDevCard) devCard.get(0);
			expectingDevCard = true;
			switch (move.getDevCardType()) {
			case PlayDevCard.MONOPOLY:
				client.playDevCard(game, SOCDevCardConstants.MONO);

				break;

			case PlayDevCard.YEAR_OF_PLENTY:
				client.playDevCard(game, SOCDevCardConstants.DISC);
				break;

			case PlayDevCard.ROAD_BUILDING:
				client.playDevCard(game, SOCDevCardConstants.ROADS);
				break;
			}
		} else {

			// Handle the trades first
			for (BotMove trade : trades) {
				processMove(trade);
			}

			// Then the dev card buying
			for (BotMove buy : buys) {
				processMove(buy);
			}

			if (!builds.isEmpty()) {
				expectingPiecePlacement = true;
				buildList = builds;
				requestPiecePlacement(buildList.get(0));
			} else {

				expectingDiceRoll = true;
				waitingForGameState = true;
				expectingMove = false;
				client.endTurn(game);

				System.out.println("");
			}
		}
	}

	/**
	 * Helper method to process enact the moves that the decision maker has
	 * decided that we should commit.
	 * 
	 * @param move
	 *            The part of the sequence of moves that we are doing.
	 */
	private void processMove(BotMove move) {

		switch (move.getMoveType()) {

		case BotMove.PIECE_PLACEMENT:
			requestPiecePlacement(move);
			break;

		case BotMove.DEV_CARD_BUY:
			requestDevCardBuy();
			break;

		case BotMove.DEV_CARD_PLACE:
			handleDevCardUse((PlayDevCard) move);
			break;

		case BotMove.TRADE:
			handleTrade((Trade) move);
			break;

		}
	}

	private void requestPiecePlacement(BotMove botMove) {
		// TODO Auto-generated method stub
		expectingPiecePlacement = true;
		PiecePlacement move = (PiecePlacement) botMove;
		client.buildRequest(game, move.getPieceType());
	}

	/**
	 * Request that the client asks the server to buy us a development card.
	 */
	private void requestDevCardBuy() {
		client.buyDevCard(game);
	}

	/**
	 * Method to handle a move which says we should place a piece on the table.
	 * The methods check what piece is being requested and then asks the server
	 * to place that piece on the game board.
	 * 
	 * @param move
	 */
	private void handlePiecePlacement(PiecePlacement move) {

		SOCPlayingPiece piece = null;

		switch (move.getPieceType()) {

		case SOCPlayingPiece.ROAD:
			piece = new SOCRoad(ourPlayer, move.getCoordinate(), game.getBoard());
			break;

		case SOCPlayingPiece.CITY:
			piece = new SOCCity(ourPlayer, move.getCoordinate(), game.getBoard());
			break;

		case SOCPlayingPiece.SETTLEMENT:
			piece = new SOCSettlement(ourPlayer, move.getCoordinate(), game.getBoard());
			break;

		}

		client.putPiece(game, piece);

	}

	/**
	 * Method to get the client to play a development card. Further interaction
	 * will be required in order to make certain decisions regarding the
	 * development card.
	 * 
	 * @param devCard
	 */
	private void handleDevCardUse(PlayDevCard devCard) {

	}

	private void handleTrade(Trade trade) {
		// If the target is the bank
		if (trade.getTradeTarget() == -1) {
			handleBankTrade(trade);
		}
	}

	private void handleBankTrade(Trade trade) {
		System.out.println(trade.getGiveType() + " " + trade.getRecType());
		SOCResourceSet giveSet = new SOCResourceSet();
		giveSet.setAmount(trade.getGiveAmount(), trade.getGiveType());

		SOCResourceSet recSet = new SOCResourceSet();
		recSet.setAmount(trade.getRecAmount(), trade.getRecType());

		client.bankTrade(game, giveSet, recSet);
	}

	/**
	 * Method to request that the client rolls the dice.
	 */
	private void requestDiceRoll() {
		client.rollDice(game);
	}

	/**
	 * Method that will handle the initial placement of both roads and
	 * settlements by passing it to a special segment which will decide this
	 * information given the state of the board
	 */
	private void doInitialPlacement(int currentState) {
		board = game.getBoard();
		int location = initialDecider.handleDecision(currentState, board);

		// If we are placing a road
		if (currentState == SOCGame.START1B || currentState == SOCGame.START2B) {
			requestRoadPlacement(location);
			waitingForGameState = true;

			// If we are placing a settlement
		} else {
			requestSettlementPlacement(location);
			waitingForGameState = true;
		}
	}

	/**
	 * Method requested that the client asks the server to place a road at a
	 * specific location.
	 * 
	 * @param location
	 */
	private void requestRoadPlacement(int location) {
		client.putPiece(game, new SOCRoad(ourPlayer, location, null));
	}

	/**
	 * Method requesting that the client asks the server to place a road at a
	 * specific location.
	 * 
	 * @param location
	 */
	private void requestSettlementPlacement(int location) {
		client.putPiece(game, new SOCSettlement(ourPlayer, location, null));
	}

	/**
	 * 
	 * @param msg
	 */
	private void handleResourceCount(SOCMessage msg) {
		SOCPlayer pl = game.getPlayer(((SOCResourceCount) msg).getPlayerNumber());

		if (pl.getPlayerNumber() == ourPlayer.getPlayerNumber()) {
			if (((SOCResourceCount) msg).getCount() != pl.getResources().getTotal()) {
				System.out.println("RESOURCE MISMATCH!!!!!!");
			}
		}
	}

	private void handlePlayerElement(SOCPlayerElement mes) {
		SOCPlayer pl = null;
		if (mes.getPlayerNumber() >= 0) {
			pl = game.getPlayer(mes.getPlayerNumber());
		}

		switch (mes.getElementType()) {
		case SOCPlayerElement.ROADS:

			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numPieces(mes, pl, SOCPlayingPiece.ROAD);
			break;

		case SOCPlayerElement.SETTLEMENTS:

			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numPieces(mes, pl, SOCPlayingPiece.SETTLEMENT);
			break;

		case SOCPlayerElement.CITIES:

			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numPieces(mes, pl, SOCPlayingPiece.CITY);
			break;

		case SOCPlayerElement.NUMKNIGHTS:

			// PLAYERELEMENT(NUMKNIGHTS) is sent after a Soldier card is played.
			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numKnights(mes, pl, game);
			break;

		case SOCPlayerElement.CLAY:

			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numRsrc(mes, pl, SOCResourceConstants.CLAY);
			break;

		case SOCPlayerElement.ORE:

			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numRsrc(mes, pl, SOCResourceConstants.ORE);
			break;

		case SOCPlayerElement.SHEEP:

			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numRsrc(mes, pl, SOCResourceConstants.SHEEP);
			break;

		case SOCPlayerElement.WHEAT:

			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numRsrc(mes, pl, SOCResourceConstants.WHEAT);
			break;

		case SOCPlayerElement.WOOD:

			SOCDisplaylessPlayerClient.handlePLAYERELEMENT_numRsrc(mes, pl, SOCResourceConstants.WOOD);
			break;
		}
	}

	/**
	 * Handle the reception of the turn method that is received from the server.
	 * If it is our turn this method will trigger a flag that indicates that it
	 * is our turn.
	 * 
	 * @param turn
	 */
	private void handleTurnMessage(SOCTurn turn) {
		game.setCurrentPlayerNumber(turn.getPlayerNumber());
		game.updateAtTurn();
		board = game.getBoard();

		if (game.getCurrentPlayerNumber() == ourPlayer.getPlayerNumber()) {
			ourTurn = true;
			expectingMove = true;
			expectingDiceRoll = true;
		} else {
			ourTurn = false;
		}

	}

	/**
	 * Set the current turn number that we are on.
	 * 
	 * @param seatNumber
	 */
	private void setTurnNumber(int seatNumber) {
		// Check that the turn number has advanced.
		if (seatNumber != currentPlayer) {
			turnCounter++;
		}
		game.setCurrentPlayerNumber(seatNumber);
	}

	/**
	 * Method to set the field indicating which player will go first.
	 * 
	 * @param playerNumber
	 *            The seat number of the player (1-4) that is going to go first.
	 */
	private void setFirstPlayer(int playerNumber) {
		this.firstPlayer = playerNumber;
	}

	/**
	 * Update the current {@link SOCGameState} that the game is in.
	 * 
	 * @param state
	 */
	private void updateGameState(int state) {
		game.setGameState(state);
		waitingForGameState = false;
	}

	/**
	 * Carry out the initialisation of the bot Need to work out what
	 * initialisation needs to go in here
	 */
	private void init() {

	}

	@Deprecated
	public void setSeatNumber(int seatNumber) {
		this.seatNumber = seatNumber;
	}

	/**
	 * Set our player data to that currently stored in the game.
	 */
	public void setPlayerData() {
		ourPlayer = game.getPlayer(client.getNickname());
		dm.setOurPlayerInformation(ourPlayer);
		initialDecider = new InitialMoveDecider(ourPlayer);
	}

	/**
	 * Helper method to insert a pause into the execution of this thread.
	 * 
	 * @param ms
	 *            The length of the pause in MS.
	 */
	private void pause(int ms) {
		try {
			yield();
			sleep(ms);
		} catch (InterruptedException exc) {
		}
	}
}
