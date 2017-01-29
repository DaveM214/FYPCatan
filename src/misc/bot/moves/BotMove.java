package misc.bot.moves;

/**
 * Class representing the the possible types of moves that we could make on our
 * turn.
 * 
 * @author david
 *
 */
public class BotMove {

	private final int moveType;

	public static final int PIECE_PLACEMENT = 1;
	public static final int TRADE = 2;
	public static final int DEV_CARD_BUY = 3;
	public static final int DEV_CARD_PLACE = 4;

	/**
	 * Constructor. Create a BotMove given its type. Its type is immutable and
	 * cannot be changed.
	 * 
	 * @param moveType
	 */
	public BotMove(int moveType) {
		this.moveType = moveType;
	}

	/**
	 * The type of move this is
	 * 
	 * @return The move type
	 */
	public int getMoveType() {
		return moveType;
	}

}
