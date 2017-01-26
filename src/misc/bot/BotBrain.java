package misc.bot;

import misc.utils.BotMessageQueue;

import java.util.Vector;

import misc.bot.BotClient;
import soc.game.SOCBoard;
import soc.game.SOCCity;
import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCPlayingPiece;
import soc.game.SOCRoad;
import soc.game.SOCSettlement;
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
 * will handle the communication with the server. We will use a similar
 * architecture where a queue is maintained
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
	private boolean initialRoadsBuilt;
	private boolean initialSettlementsBuilt;

	// Possible playing strategy constants
	public final static int BRICK_STRATEGY = 1;
	public final static int ORE_STRATEGY = 2;
	public final static int MIXED_STRATEGY = 3;

	private InitialMoveDecider decider;
	private boolean expectingMove = false;
	private boolean waitingForGameState = false;
	private boolean expectingDiceRoll = false;

	public BotBrain(BotClient client, SOCGame game, BotMessageQueue<SOCMessage> msgQueue) {
		msgQ = msgQueue;
		this.client = client;
		this.game = game;
		alive = true;
		dm = new MixedDecisionMaker(game, ourPlayer);
	}

	// TODO complete this method.
	// Needs to kill the bot. Maybe add some feedback to the client?
	public void kill() {

	}

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
					handePlayerElement(((SOCPlayerElement) msg));
					break;

				// Handle the message showing how many resources a player has.
				case SOCMessage.RESOURCECOUNT:
					handleResourceCount(msg);
					break;

				case SOCMessage.PUTPIECE:
					handlePutPiece((SOCPutPiece) msg);
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

			}
		}
	}

	private void handlePutPiece(SOCPutPiece msg) {
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

	private void handleOurTurn() {
		int currentState = game.getGameState();

		// IF Initial state
		if (currentState >= 5 && currentState <= 11) {
			// We will do initial placement manually through probabilities and
			// stats
			doInitialPlacement(currentState);
			expectingDiceRoll = true;
			
		}
		// Otherwise we are in the main game.
		else {
			if (expectingDiceRoll == true) {
				System.out.println("ROLLING DICE!");
				requestDiceRoll();
				expectingDiceRoll = false;
			}else{
				//Ask our decision maker to give us a set of moves.
				dm.getMoveDecision();
				expectingDiceRoll = true;
				waitingForGameState = true;
				client.endTurn(game);			
			}
		}

	}

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
		int location = decider.handleDecision(currentState, board);

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

	private void requestRoadPlacement(int location) {
		client.putPiece(game, new SOCRoad(ourPlayer, location, null));
	}

	private void requestSettlementPlacement(int location) {
		client.putPiece(game, new SOCSettlement(ourPlayer, location, null));
	}

	private void handleResourceCount(SOCMessage msg) {
		SOCPlayer pl = game.getPlayer(((SOCResourceCount) msg).getPlayerNumber());
		if (((SOCResourceCount) msg).getCount() != pl.getResources().getTotal()) {
			// TODO handle mistmatches
		}
	}

	private void handePlayerElement(SOCPlayerElement socPlayerElement) {
	
	}

	private void handleTurnMessage(SOCTurn turn) {
		game.setCurrentPlayerNumber(turn.getPlayerNumber());
		game.updateAtTurn();
		board = game.getBoard();

		if (game.getCurrentPlayerNumber() == ourPlayer.getPlayerNumber()) {
			ourTurn = true;
			expectingMove = true;
		} else {
			ourTurn = false;
		}

		// TODO code here to enact our turn
	}

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

	public void setPlayerData() {
		ourPlayer = game.getPlayer(client.getNickname());
		decider = new InitialMoveDecider(ourPlayer);
	}

	private void pause(int ms) {
		try {
			yield();
			sleep(ms);
		} catch (InterruptedException exc) {
		}
	}

	// TODO Implement the behaviours of this brain and how it will be organised,
	// work out how the loop will work in the runnable.

	// TODO work out how the ASP approach will be integrated into this.
}
