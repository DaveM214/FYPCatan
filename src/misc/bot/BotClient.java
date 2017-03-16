package misc.bot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

import misc.utils.BotMessageQueue;
import misc.utils.exceptions.QSizeExceededException;
import soc.disableDebug.D;
import soc.game.SOCGame;
import soc.game.SOCGameOption;
import soc.message.SOCAdminPing;
import soc.message.SOCAdminReset;
import soc.message.SOCChangeFace;
import soc.message.SOCDeleteGame;
import soc.message.SOCGameMembers;
import soc.message.SOCGameState;
import soc.message.SOCGameTextMsg;
import soc.message.SOCImARobot;
import soc.message.SOCInventoryItemAction;
import soc.message.SOCJoinGame;
import soc.message.SOCJoinGameAuth;
import soc.message.SOCMessage;
import soc.message.SOCMessageForGame;
import soc.message.SOCPutPiece;
import soc.message.SOCResetBoardAuth;
import soc.message.SOCRobotDismiss;
import soc.message.SOCRobotJoinGameRequest;
import soc.message.SOCSetSpecialItem;
import soc.message.SOCSimpleAction;
import soc.message.SOCSimpleRequest;
import soc.message.SOCSitDown;
import soc.message.SOCStatusMessage;
import soc.message.SOCUpdateRobotParams;
import soc.message.SOCVersion;
import soc.robot.SOCRobotBrain;
import soc.robot.SOCRobotClient;
import soc.robot.SOCRobotDM;
import soc.server.genericServer.LocalStringServerSocket;
import soc.util.CappedQueue;
import soc.util.CutoffExceededException;
import soc.util.SOCRobotParameters;
import soc.util.SOCServerFeatures;
import soc.util.Version;

/**
 * Extension of the {@link SOCRobotClient} class from the JSettlers API with
 * various changes and overrides in order to suit our needs. This class is
 * responsible for establishing a client which connects to the server. A client
 * is capable of running many games at the same time on a server and is
 * responsible for distributing the messages received from the server to the
 * correct bot playing the game.
 * 
 * @author david
 *
 */
public class BotClient extends SOCRobotClient {

	private static final String HOST_NAME = "127.0.0.1";
	private static final int PORT = 8880;
	private static final String NICKNAME = "CatanBot";
	private static final String PASSWORD = "password";
	private static final String COOKIE = "cookie";
	private static final int SERVER_VERSION_NUMBER = 2000;
	private static final String SERVER_VERSION_STRING = "2.0.00";
	private static final String SERVER_BUILD = "JM20161228";
	private Thread readerRobot;
	
	private static final String STATS_PATH = "statistics.txt";
	private static final boolean APPEND_TO_FILE = true;
	private FileWriter writer;
	
	// Hash table containing the brains of games
	private Hashtable<String, BotBrain> myRobotBrains = new Hashtable<String, BotBrain>();

	// Hash table containing the queue for multiple brains - key is game
	private Hashtable<String, BotMessageQueue<SOCMessage>> brainQueues = new Hashtable<String, BotMessageQueue<SOCMessage>>();

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
		initFileWriter();
	}

	private void initFileWriter() {
		try {
			writer = new FileWriter(STATS_PATH,APPEND_TO_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Constructor for connecting to a local game (practice) on a local
	 * stringport.
	 *
	 * @param s
	 *            the stringport that the server listens on
	 * @param nn
	 *            nickname for robot
	 * @param pw
	 *            password for robot
	 * @param co
	 *            cookie for robot connections to server
	 */
	public BotClient(String s, String nn, String pw, String co) {
		super(s, nn, pw, co);
	}

	/**
	 * Override the init method to avoid having to explicitly deal with the
	 * dynamic versioning that will mean extra resource files need to be
	 * included. Doing this should keep the required libraries to run the game
	 * as small as possible
	 */
	@Override
	public void init() {
		try {
			if (strSocketName == null) {
				s = new Socket(host, port);
				s.setSoTimeout(300000);
				in = new DataInputStream(s.getInputStream());
				out = new DataOutputStream(s.getOutputStream());
			} else {
				sLocal = LocalStringServerSocket.connectTo(strSocketName);
			}
			connected = true;
			readerRobot = new Thread(this);
			readerRobot.start();

			// resetThread = new SOCRobotResetThread(this);
			// resetThread.start();
			put(SOCVersion.toCmd(SERVER_VERSION_NUMBER, SERVER_VERSION_STRING, SERVER_BUILD, null));
			put(SOCImARobot.toCmd(nickname, COOKIE, "CATANBOT"));
		} catch (Exception e) {
			ex = e;
			System.err.println("Could not connect to the server: " + ex);
		}
	}

	/**
	 * Override of the treat method because we will want to handle the messages
	 * that come from the server differently than the way the AI that comes with
	 * JSettlers deals with them although a lot of them will probably be the
	 * same and will borrow heavily from the method
	 * 
	 * Some of the handling will be the same as the base SOCRobotClient and this
	 * case it will be possible to simply "deflect" back the
	 * 
	 * We may need to add some of the debugging functionality back in later.
	 */
	@Override
	public void treat(SOCMessage msg) {
		if (msg == null) {
			return;
		}

		final String message = msg.toString();
		// System.out.println("Msg Received: " + message);

		try {
			switch (msg.getType()) {

			// All of these messages go directly to the games "brain" to deal
			// with.
			case SOCMessage.ACCEPTOFFER:
			case SOCMessage.CANCELBUILDREQUEST:
			case SOCMessage.CHOOSEPLAYER:
			case SOCMessage.CHOOSEPLAYERREQUEST:
			case SOCMessage.CLEAROFFER:
			case SOCMessage.DEVCARDACTION:
			case SOCMessage.DEVCARDCOUNT:
			case SOCMessage.DICERESULT:
			case SOCMessage.DISCARDREQUEST:
			case SOCMessage.FIRSTPLAYER:
			case SOCMessage.MAKEOFFER:
			case SOCMessage.MOVEPIECE:
			case SOCMessage.MOVEROBBER:
			case SOCMessage.PICKRESOURCESREQUEST:
			case SOCMessage.PLAYERELEMENT:
			case SOCMessage.REJECTOFFER:
			case SOCMessage.RESOURCECOUNT:
			case SOCMessage.SETPLAYEDDEVCARD:
			case SOCMessage.SETTURN:
			case SOCMessage.TIMINGPING: // server's 1x/second timing ping
			case SOCMessage.TURN:
				addMessageToBrainQueue((SOCMessageForGame) msg);
				break;

			// All of these messages need to have their handler carefully
			// considered to see whether we need to change the behaviour in
			// order to work with our bot.

			// No override - use default
			case SOCMessage.STATUSMESSAGE:
				handleSTATUSMESSAGE((SOCStatusMessage) msg);
				break;

			/**
			 * admin ping
			 */
			// No override - use default
			case SOCMessage.ADMINPING:
				handleADMINPING((SOCAdminPing) msg);
				break;

			/**
			 * admin reset
			 */
			// No override - use default
			case SOCMessage.ADMINRESET:
				handleADMINRESET((SOCAdminReset) msg);
				break;

			/**
			 * update the current robot parameters
			 */
			// No override - use default
			case SOCMessage.UPDATEROBOTPARAMS:
				handleUPDATEROBOTPARAMS((SOCUpdateRobotParams) msg);
				break;

			/**
			 * join game authorization
			 */
			// This one is overridden!
			case SOCMessage.JOINGAMEAUTH:
				handleJOINGAMEAUTH((SOCJoinGameAuth) msg, (sLocal != null));
				break;

			/**
			 * game has been destroyed
			 */
			// This one is overridden!
			case SOCMessage.DELETEGAME:
				handleDELETEGAME((SOCDeleteGame) msg);
				break;

			/**
			 * list of game members
			 */
			// No override - use default
			case SOCMessage.GAMEMEMBERS:
				handleGAMEMEMBERS((SOCGameMembers) msg);
				break;

			/**
			 * game text message
			 */
			// Overridden
			case SOCMessage.GAMETEXTMSG:
				handleGAMETEXTMSG((SOCGameTextMsg) msg);
				break;

			/**
			 * someone is sitting down
			 */
			// Overridden
			case SOCMessage.SITDOWN:
				handleSITDOWN((SOCSitDown) msg);
				break;

			/**
			 * update the state of the game
			 */
			// Overridden
			case SOCMessage.GAMESTATE:
				handleGAMESTATE((SOCGameState) msg);
				break;

			/**
			 * a player built something
			 */
			// Overridden
			case SOCMessage.PUTPIECE:
				handlePUTPIECE((SOCPutPiece) msg);
				break;

			/**
			 * the server is requesting that we join a game
			 */
			// Not Overridden
			case SOCMessage.ROBOTJOINGAMEREQUEST:
				handleROBOTJOINGAMEREQUEST((SOCRobotJoinGameRequest) msg);
				break;

			/**
			 * message that means the server wants us to leave the game
			 */
			// Overridden
			case SOCMessage.ROBOTDISMISS:
				handleROBOTDISMISS((SOCRobotDismiss) msg);
				break;

			case SOCMessage.VERSION:
				handleVERSION((sLocal != null), (SOCVersion) msg);
				break;

			/**
			 * handle board reset (new game with same players, same game name,
			 * new layout).
			 */
			// Overridden
			case SOCMessage.RESETBOARDAUTH:
				handleRESETBOARDAUTH((SOCResetBoardAuth) msg);
				break;

			/**
			 * generic "simple request" responses or announcements from the
			 * server. Message type added 2013-02-17 for v1.1.18, bot ignored
			 * these until 2015-10-10 for v2.0.00 SC_PIRI.
			 */
			case SOCMessage.SIMPLEREQUEST:
				super.handleSIMPLEREQUEST(games, (SOCSimpleRequest) msg);
				addMessageToBrainQueue((SOCSimpleRequest) msg);
				break;

			/**
			 * generic "simple action" announcements from the server. Added
			 * 2013-09-04 for v1.1.19.
			 */
			case SOCMessage.SIMPLEACTION:
				super.handleSIMPLEACTION(games, (SOCSimpleAction) msg);
				addMessageToBrainQueue((SOCSimpleAction) msg);
				break;

			/**
			 * a special inventory item action: either add or remove, or we
			 * cannot play our requested item. Added 2013-11-26 for v2.0.00.
			 */
			case SOCMessage.INVENTORYITEMACTION: {
				final boolean isReject = super.handleINVENTORYITEMACTION(games, (SOCInventoryItemAction) msg);
				if (isReject)
					addMessageToBrainQueue((SOCInventoryItemAction) msg);
			}
				break;

			/**
			 * Special Item change announcements. Added 2014-04-16 for v2.0.00.
			 */
			case SOCMessage.SETSPECIALITEM:
				super.handleSETSPECIALITEM(games, (SOCSetSpecialItem) msg);
				addMessageToBrainQueue((SOCSetSpecialItem) msg);
				break;

			// Irrelevant to robots and "one-directional" so can simply be
			// binned.
			case SOCMessage.BCASTTEXTMSG:
			case SOCMessage.CHANGEFACE:
			case SOCMessage.CHANNELMEMBERS:
			case SOCMessage.CHANNELS: // If bot ever uses CHANNELS, update
										// SOCChannels class javadoc
			case SOCMessage.DELETECHANNEL:
			case SOCMessage.GAMES:
			case SOCMessage.GAMESERVERTEXT: // SOCGameServerText contents are
											// ignored by bots
			case SOCMessage.GAMESTATS:
			case SOCMessage.JOINCHANNEL:
			case SOCMessage.JOINCHANNELAUTH:
			case SOCMessage.LEAVECHANNEL:
			case SOCMessage.NEWCHANNEL:
			case SOCMessage.NEWGAME:
			case SOCMessage.SETSEATLOCK:
			case SOCMessage.TEXTMSG:
				break;

			// Default case which will be for all of the messages that we are
			// not interested in, these can simply be ignored by passing them to
			// the superclasses method that will gracefully ignore them.
			default:
				super.treat(msg, false);
			}
		} catch (Throwable e) {

		}

	}

	/**
	 * Add a message to the queue of messages that need to be processed by the
	 * AI brain which contain the elements that decide what moves we are doing.
	 * 
	 * @param msg
	 *            The message being added to the queue.
	 */
	public void addMessageToBrainQueue(SOCMessageForGame msg) {
		BotMessageQueue<SOCMessage> brainQueue = brainQueues.get(msg.getGame());
		if (brainQueue != null) {

			try {
				brainQueue.put((SOCMessage) msg);
			} catch (QSizeExceededException e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * Handles the {@link SOCJoinGameAuth} message. This message allows a player
	 * to join a game. This method is responsible for the creation of the
	 * specific instances of {@link BotBrain} that play then play the game.
	 */
	@Override
	public void handleJOINGAMEAUTH(SOCJoinGameAuth msg, boolean isPractice) {
		gamesPlayed++;
		final String gaName = msg.getGame();
		SOCGame ga = new SOCGame(gaName, true, gameOptions.get(gaName));
		ga.isPractice = isPractice;
		games.put(gaName, ga);

		BotMessageQueue<SOCMessage> brainQ = new BotMessageQueue<SOCMessage>();
		brainQueues.put(gaName, brainQ);

		BotBrain brain = new BotBrain(this, ga, brainQ);
		myRobotBrains.put(gaName, brain);
	}

	/**
	 * Method to handle deletion of a game. If this game is over then add stats
	 * and such
	 * 
	 */
	@Override
	protected void handleDELETEGAME(SOCDeleteGame mes) {
		BotBrain brain = myRobotBrains.get(mes.getGame());

		if (brain != null) {
			SOCGame ga = games.get(mes.getGame());

			if (ga != null) {
				if (ga.getGameState() == SOCGame.OVER) {
					gamesFinished++;

					if (ga.getPlayer(nickname).getTotalVP() >= ga.vp_winner) {
						gamesWon++;
						// TODO: should check actual winning player number
						// (getCurrentPlayerNumber?)
					}
				}

				brain.kill();
				myRobotBrains.remove(mes.getGame());
				brainQueues.remove(mes.getGame());
				games.remove(mes.getGame());
			}
		}
	}

	/**
	 * Method that will be called by the BotBrain when we reach and end of game
	 * scenario.
	 */
	public void updateStatistics(int position, int score) {
		String positionString = Integer.toString(position);
		String scoreString = Integer.toString(score);
		try {
			
			writer.flush();
			writer.write("\n" + positionString + " " + scoreString);
			writer.flush();
			
			System.out.println("GAME RECORDED! " + positionString + " " +  scoreString );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * handle the "game text message" message
	 * 
	 * @param mes
	 *            the message
	 */
	@Override
	public void handleGAMETEXTMSG(SOCGameTextMsg msg) {
		// D.ebugPrintln(mes.getNickname()+": "+mes.getText());

		// If the msg starts with the nickname of the bot then we handle the
		// message specially
		if (msg.getText().startsWith(nickname)) {
			handleGAMETEXTMSG_debug(msg);
		}

		addMessageToBrainQueue(msg);
	}

	/**
	 * Method for carrying out special commands and debug if a text message in
	 * the game begins with the name our bot.
	 */
	public void handleGAMETEXTMSG_debug(SOCGameTextMsg msg) {
		// TODO decide what functions this method will have.
		// This can be done somewhat later
	}

	/**
	 * Method to seat a player at the game. This method handles adding them to
	 * the local representation of the game. If the number of the player being
	 * seated by the game is that of our player then we will start the
	 * corresponding {@link BotBrain}
	 * 
	 */
	public void handleSITDOWN(SOCSitDown msg) {
		SOCGame ga = games.get(msg.getGame());

		/// Check the game to make sure it isn't null
		if (ga != null) {
			// Add the player to the game
			ga.addPlayer(msg.getNickname(), msg.getPlayerNumber());
			ga.getPlayer(msg.getPlayerNumber()).setRobotFlag(msg.isRobot(), false);

			// If this message is mirroring that it is us that has joined.
			if (nickname.equals(msg.getNickname())) {
				BotBrain brain = myRobotBrains.get(msg.getGame());
				// brain.setSeatNumber(msg.getPlayerNumber());
				// Start the brain.
				brain.setPlayerData();
				brain.start();
				// Put the robot face
				put(SOCChangeFace.toCmd(ga.getName(), msg.getPlayerNumber(), 0));
			}
		}
	}

	/**
	 * Handle the updating of the game state. This message is passed to the
	 * correct bot queue in the matching game mentioned from the message.
	 */
	protected void handleGAMESTATE(SOCGameState msg) {
		SOCGame ga = games.get(msg.getGame());

		if (ga != null) {
			addMessageToBrainQueue(msg);
		}
	}

	/**
	 * Handle {@link SOCPutPiece} message. This message is passed to the correct
	 * bot in the corresponding game as it is solely responsible for responding
	 * to this message.
	 * 
	 */
	protected void handlePUTPIECE(SOCPutPiece msg) {
		BotMessageQueue<SOCMessage> brainQueue = brainQueues.get(msg.getGame());
		if (brainQueue != null) {
			try {
				brainQueue.put((SOCMessage) msg);
			} catch (QSizeExceededException e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * Handle the {@link SOCRobotDismiss} message.
	 */
	protected void handleROBOTDISMISS(SOCRobotDismiss msg) {
		SOCGame ga = games.get(msg.getGame());
		BotMessageQueue<SOCMessage> brainQueue = brainQueues.get(msg.getGame());

		if ((ga != null) && (brainQueue != null)) {
			try {
				brainQueue.put(msg);
			} catch (QSizeExceededException exc) {
				D.ebugPrintln("CutoffExceededException" + exc);
			}

			/**
			 * if the brain isn't alive, then we need to leave the game here,
			 * instead of having the brain leave it
			 */
			BotBrain brain = myRobotBrains.get(msg.getGame());

			if ((brain == null) || (!brain.isAlive())) {
				leaveGame(games.get(msg.getGame()), "brain not alive in handleROBOTDISMISS", true, false);
			}
		}
	}

	/**
	 * handle the {@link SOCResetBoardAuth} message.
	 */
	@Override
	protected void handleRESETBOARDAUTH(SOCResetBoardAuth msg) {
		D.ebugPrintln("**** handleRESETBOARDAUTH ****");

		String gname = msg.getGame();
		SOCGame ga = games.get(gname);
		if (ga == null) {
			return; // Not one of our games
		}

		BotBrain brain = myRobotBrains.get(gname);
		if (brain != null)
			brain.kill();
		leaveGame(ga, "resetboardauth", false, false); // Same as in
														// handleROBOTDISMISS
		ga.destroyGame();
	}

	/**
	 * Override of the version handling method to allow us to not need to
	 * dynamically retrieve the version information of the server from a
	 * resource file. There version of the client that we can connect too will
	 * be stored in this class.
	 */
	protected void handleVERSION(boolean isLocal, SOCVersion msg) {
		int vers = msg.getVersionNumber();
		final SOCServerFeatures feats = (vers >= SOCServerFeatures.VERSION_FOR_SERVERFEATURES)
				? new SOCServerFeatures(msg.localeOrFeats) : new SOCServerFeatures(true);

		if (isLocal) {
			sLocalVersion = vers;
			sLocalFeatures = feats;
		} else {
			sVersion = vers;
			sFeatures = feats;
		}

		final int ourVers = SERVER_VERSION_NUMBER;
		if (vers != ourVers) {
			final String errmsg = "Internal error SOCDisplaylessPlayerClient.handleVERSION: Server must be same as our version "
					+ ourVers + ", not " + vers; // i18n: Unlikely error, keep
													// un-localized for possible
													// bug reporting
			System.err.println(errmsg);
			ex = new IllegalStateException(errmsg);
			destroy();
		}
	}

}
