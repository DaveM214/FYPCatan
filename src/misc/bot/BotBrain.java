package misc.bot;

import misc.utils.BotMessageQueue;
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

	public BotBrain(BotClient client, SOCGame game, BotMessageQueue msgQueue) {

	}

	// TODO complete this method.
	// Needs to kill the bot. Maybe add some feedback to the client?
	public void kill() {

	}

	@Override
	public void run() {

	}
	// TODO Implement the behaviours of this brain and how it will be organised,
	// work out how the loop will work in the runnable.

	// TODO work out how the ASP approach will be integrated into this.
}
