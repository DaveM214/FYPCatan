package misc.bot;

import java.util.Hashtable;

import soc.robot.SOCRobotClient;
import soc.util.SOCRobotParameters;

/**
 * Class extends the robot class
 * 
 * @author david
 *
 */
public class BotClient extends SOCRobotClient {

	private static final String HOST_NAME = "localhost";
	private static final int PORT = 8088;
	private static final String NICKNAME = "CatanBot";
	private static final String PASSWORD = "password";
	private static final String COOKIE = "cookie";
	private Hashtable<String, BotBrain> myRobotBrains = new Hashtable<String, BotBrain>();

	/**
	 * Default constructor. Uses the default values stored in the class.
	 */
	public BotClient() {
		this(HOST_NAME, PORT, NICKNAME, PASSWORD, COOKIE);
	}

	/**
	 * Constructor. Calls constructor of super class to construct a bot client
	 * ready to connect to a JSettlers server
	 * 
	 * @param host
	 *            The hostname of the server
	 * @param port
	 *            Port number of the server
	 * @param nick
	 *            The name the robot will take
	 * @param pass
	 *            The password for the sever
	 * @param cookie
	 *            The cookie the server wants for security purposes
	 */
	public BotClient(String host, int port, String nick, String pass, String cookie) {
		super(host, port, nick, pass, cookie);

	}
	// TODO Over-write the methods that are to do with the old version of the
	// brain used to direct the AI

	// TODO Implement method for running multiple games after one another.
	// Probably take an input from the main application class

}
