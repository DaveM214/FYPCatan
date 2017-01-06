package misc.application;

import misc.bot.BotClient;

/**
 * Class with main method representing the application that will allow bots to
 * be created
 * 
 * @author david
 *
 */
public class BotApplication {

	private static final String WELCOME_MESSAGE = "Starting the bot client and connecting to server...";

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		printWelcome();
		BotClient client = new BotClient();
		client.init();
		System.out.println("test"); // Test to see if blocked and we need to put
									// this in another thread
	}

	private static void printWelcome() {
		System.out.println(WELCOME_MESSAGE);
	}

	// TODO allow a specific port number / hostname to be input into the args
	// TODO Allow user to direct the options of program.
	// TODO Diagram of the program

}
