package misc.bot;

import misc.utils.BotMessageQueue;
import misc.bot.BotClient;
import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.message.SOCFirstPlayer;
import soc.message.SOCGameState;
import soc.message.SOCMessage;
import soc.message.SOCSetTurn;

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
	private BotClient client;
	private SOCBoard board;
	private SOCGame game;
	private boolean boardInit = false;
	private int seatNumber;
	private boolean alive;
	private int currentPlayer;
	private int firstPlayer;
	private int turnCounter; //Number of turns there have been
	private int ourTurnCounter; //Number of turns we have had
	private boolean initialRoadsBuilt;
	private boolean initialSettlementsBuilt;
	
	public BotBrain(BotClient client, SOCGame game, BotMessageQueue<SOCMessage> msgQueue) {
		msgQ = msgQueue;
		this.client = client;
		this.game = game;
		alive = true;
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
				
				switch (msgType) {
				
				//We have received a new game state from the server.
				case SOCMessage.GAMESTATE:
					updateGameState(((SOCGameState) msg).getState());
					break;
					
				//Handle the message that indicates who the first player to move is.	
				case SOCMessage.FIRSTPLAYER	:
					setFirstPlayer(((SOCFirstPlayer)msg).getPlayerNumber());
					break;
					
				case SOCMessage.SETTURN:
					setTurnNumber((((SOCSetTurn)msg).getPlayerNumber()));
					break;
					
				case SOCMessage.TURN:
					setTurn
					break;
					
				default:
					break;
				}

			} catch (Exception e) {

			}
		}
	}
	
	private void setTurnNumber(int seatNumber) {
		//Check that the turn number has advanced.
		if(seatNumber != currentPlayer){
			turnCounter++;
		}
		game.setCurrentPlayerNumber(seatNumber);
	}

	/**
	 * Method to set the field indicating which player will go first.
	 * @param playerNumber The seat number of the player (1-4) that is going to go first.
	 */
	private void setFirstPlayer(int playerNumber) {
		this.firstPlayer = playerNumber;
	}

	//TODO Code to update state of the game.
	private void updateGameState(int state) {
		
	}

	/**
	 * Carry out the initialisation of the bot Need to work out what
	 * initialisation needs to go in here
	 */
	private void init() {

	}

	public void setSeatNumber(int seatNumber) {
		this.seatNumber = seatNumber;
	}

	// TODO Implement the behaviours of this brain and how it will be organised,
	// work out how the loop will work in the runnable.

	// TODO work out how the ASP approach will be integrated into this.
}
