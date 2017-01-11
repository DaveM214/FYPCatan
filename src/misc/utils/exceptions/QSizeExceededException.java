package misc.utils.exceptions;

/**
 * Exception we throw if the queue of messages to the bot is too large. This
 * would indicate that the bot is processing messages correctly
 * 
 * @author david
 *
 */
public class QSizeExceededException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public QSizeExceededException() {
		// TODO Auto-generated constructor stub
	}

}
