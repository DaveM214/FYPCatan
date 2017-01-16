package misc.bot;

import misc.utils.BotMessageQueue;
import misc.bot.BotClient;
import soc.game.SOCBoard;
import soc.game.SOCGame;

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
	
	private BotMessageQueue msgQ;
	private BotClient client;
	private SOCBoard board; 
	private SOCGame game;
	private boolean boardInit = false;
	private int seatNumber;

	public BotBrain(BotClient client, SOCGame game, BotMessageQueue msgQueue) {
		msgQ = msgQueue;
		this.client = client;
		this.game = game;
	}

	// TODO complete this method.
	// Needs to kill the bot. Maybe add some feedback to the client?
	public void kill() {

	}

	@Override
	public void run() {
		init();
	}
	
	/**
	 * Carry out the initialisation of the bot
	 * Need to work out what initialisation needs to go in here
	 */
	private void init(){
		
		
	}
	
	public void setSeatNumber(int seatNumber){
		this.seatNumber = seatNumber;
	}
	
	// TODO Implement the behaviours of this brain and how it will be organised,
	// work out how the loop will work in the runnable.

	// TODO work out how the ASP approach will be integrated into this.
}
