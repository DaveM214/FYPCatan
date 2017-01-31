package misc.bot.moves;

import soc.game.SOCResourceConstants;

/**
 * Class that represents the move if playing a year of plenty development card.
 * When we play this particular card we are allowed to pick any two resource
 * cards we want from the bank. This class is meant to be "immutable" the
 * details of it cannot be changed after it has been constructed.
 * 
 * @author david
 *
 */
public class PlayYOP extends PlayDevCard {

	private final int resource1;
	private final int resource2;

	/**
	 * Constructor. The arguments it takes are the integers representing the two
	 * resource from {@link SOCResourceConstants}.
	 * 
	 * @param resource1 First {@link SOCResourceConstants} we want. 
	 * @param resource2 Second {@link SOCResourceConstants} we want.
	 */
	public PlayYOP(int resource1, int resource2) {
		super(PlayDevCard.YEAR_OF_PLENTY);
		this.resource1 = resource1;
		this.resource2 = resource2;
	}

	public int getResource1() {
		return resource1;
	}

	public int getResource2() {
		return resource2;
	}

}
