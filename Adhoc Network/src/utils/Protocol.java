package utils;

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
	 * Source address
	 */
	public static final int SOURCE = 2;

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
	public static final short MAXHOPS = 4;

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
}
