package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Protocol used when communicating between clients
 *
 * @author Frank
 */
public class Protocol {

	/**
	 * Name for the main chat tab
	 */
	public static final String MAINCHAT = "Chatroom";

	/**
	 * @return The source address as an integer
	 */
	public static int getSourceAddress() {
		try {
			return ((InetAddress.getLocalHost().getAddress() [0] & 0xFF) << (3*8)) +
                    ((InetAddress.getLocalHost().getAddress() [1] & 0xFF) << (2*8)) +
                    ((InetAddress.getLocalHost().getAddress() [2] & 0xFF) << (1*8)) +
                    (InetAddress.getLocalHost().getAddress() [3] &  0xFF) + 1;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return -1;
	}

	/**
	 * Max amount of milliseconds that a user can be inactive
	 */
	public static final long INACTIVITY_LIMIT = 9000l;

	/**
	 * Timeout time for retransmissions in milliseconds
	 */
	public static final long TIMEOUT = 100l;

	/**
	 * Rate at which the 'alive' messages should be broadcasted
	 */
	public static final long ALIVE_RATE = 3000l;

	/**
	 * Broadcast address
	 */
	public static final int BROADCAST = 0;
	
	/**
	 * Max hops
	 */
	public static final short MAXHOPS = 3;

	/**
	 * Private chat protocol message
	 */
	public static final String PRIVCHAT = "PRIV";

	/**
	 * Gui notification protocol message
	 */
	public static final String NOTIFY = "NOTIFY";

	/**
	 * ALIVE protocol message to let other clients know we're still here
	 */
	public static final String ALIVE = "ALIVE";

	/**
	 * PART protocol message used to signal a user leaving the chat
	 */
	public static final String PART = "PART";

	/**
	 * NAME_CHANGE protocol message for signaling a name change
	 */
	public static final String NAME_CHANGE = "NMCHG";

	/**
	 * PUB_KEY protocol message for signalling a public key
	 */
	public static final String PUB_KEY = "PUB";

	/**
	 * SYM_KEY protocol message for signalling a symmetric key
	 */
	public static final String SYM_KEY = "SYM";

	/**
	 * KEY_RECEIVED protocol message for signalling a successful key exchange
	 */
	public static final String KEY_RECEIVED = "KEYRECV";
}
