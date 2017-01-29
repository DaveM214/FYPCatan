package misc.utils;

import java.util.Vector;

import misc.utils.exceptions.QSizeExceededException;
import soc.util.CappedQueue;
import soc.util.CutoffExceededException;

/**
 * Class inspired by the {@link CappedQueue} in JSettlers. Ideally a thread
 * safe(ish) queue that will allow messages to be added to up to a maximum size.
 * The messages added to the server will be added to this queue. This class was
 * implemented freshly rather than reusing the similar JSettlers code due to the
 * likelihood of wanting some additional functionality in the future.
 * 
 * @author david
 *
 */
public class BotMessageQueue<T> {

	private static final int DEFAULT_SIZE_LIMIT = 3000;
	private final int sizeLimit;
	private Vector<T> messages = new Vector<T>();

	/**
	 * Constructor. Creates a message queue of size specified in the parameters.
	 * 
	 * @param maxSize
	 *            The maximum permitted size of the message queue.
	 */
	public BotMessageQueue(int maxSize) {
		sizeLimit = maxSize;
	}

	/**
	 * Constructor. Creates a message queue of the default size
	 */
	public BotMessageQueue() {
		sizeLimit = DEFAULT_SIZE_LIMIT;
	}

	/**
	 * Put an element at the end of the list. If the list is full then throw an
	 * exception.
	 */
	synchronized public void put(T element) throws QSizeExceededException {
		messages.add(element);
		notifyAll();

		if (messages.size() == sizeLimit) {
			throw new QSizeExceededException();
		}
	}

	/**
	 * Return the first element in the queue and remove it. If there are no
	 * elements then wait until one is added to the list and try adding it
	 * unless we are interrupted.
	 */
	synchronized public T get() {

		while (true) {
			if (messages.size() > 0) {
				T element = messages.firstElement();
				messages.remove(0);
				return element;
			} else {
				try {
					wait();
				} catch (InterruptedException e) {

				}
			}
		}
	}

	/**
	 * Get the number of messages currently stored in the queue. This will be 0
	 * if the queue is empty.
	 * 
	 * @return The size of the queue
	 */
	synchronized public int getSize() {
		return messages.size();
	}

	/**
	 * Check if the queue is empty.
	 */
	synchronized public boolean isEmpty() {
		return messages.isEmpty();
	}
}
