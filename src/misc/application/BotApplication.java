package misc.application;

import misc.bot.BotClient;

/**
 * Class with main method representing the application that will allow bots to
 * be created. This method will also handle the startup parameters that allow
 * multiple games to be started
* 
 * @author david
 *
 */
public class BotApplication {

	private static final String WELCOME_MESSAGE = "Starting the bot client and connecting to server...";

	/**
	 * Main method. Starts and initialises the bot client that will interact
	 * with the JSettlers server.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		printWelcome();
		BotClient client = new BotClient();
		client.init();
	}

	/**
	 * Helper method to print message on startup
	 */
	private static void printWelcome() {
		System.out.println(WELCOME_MESSAGE);
	}

	// TODO allow a specific port number / hostname to be input into the args
	// TODO Allow user to direct the options of program.
	// TODO Diagram of the program

}
