package misc.bot;

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
import soc.robot.SOCRobotBrain;
import soc.robot.SOCRobotClient;
import soc.robot.SOCRobotDM;
import soc.util.CappedQueue;
import soc.util.CutoffExceededException;
import soc.util.SOCRobotParameters;

/**
 * Class extends the robot class
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
		System.out.println("Msg Received: " + message);
		System.out.println(msg);

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

	// This method creates the robot brain.
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

	@Override
	public void handleDELETEGAME(SOCDeleteGame mes) {
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
	 * the local representation of the game.
	 * 
	 */
	public void handleSITDOWN(SOCSitDown msg) {
		SOCGame ga = games.get(msg.getGame());

		/// Check the game to make sure it isn't null
		if (ga != null) {
			// Add the player to the game
			ga.addPlayer(msg.getNickname(), msg.getPlayerNumber());
		}

		// If this message is mirroring that it is us that has joined.
		if (nickname.equals(msg.getNickname())) {
			BotBrain brain = myRobotBrains.get(msg.getGame());

			// Start the brain.
			brain.start();
			// Put the robot face
			put(SOCChangeFace.toCmd(ga.getName(), msg.getPlayerNumber(), 0));
		}
		// TODO possible code here if you want to build trackers for the
		// opposition players that are joining
	}

	// This message should be handed to the brain queue
	public void handleGAMESTATE(SOCGameState msg) {
		SOCGame ga = games.get(msg.getGame());

		if (ga != null) {
			addMessageToBrainQueue(msg);
		}
	}

	public void handlePUTPIECE(SOCPutPiece msg) {
		BotMessageQueue<SOCMessage> brainQueue = brainQueues.get(msg.getGame());
		if (brainQueue != null) {

			try {
				brainQueue.put((SOCMessage) msg);
			} catch (QSizeExceededException e) {
				// TODO: handle exception
			}
		}
	}

	// Handle the robot being dismissed from the game.
	public void handleROBOTDISMISS(SOCRobotDismiss msg) {
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

	@Override
	public void handleRESETBOARDAUTH(SOCResetBoardAuth msg) {
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

	// TODO Implement method for running multiple games after one another.
	// Probably take an input from the main application class

}
