package utils;

public class Protocol {

	/**
	 * Name for the main chat tab
	 */
	public static final String MAINCHAT = "Chatroom";

	/**
	 * Source address
	 */
	public static final int SOURCE = 1;

	/**
	 * Max amount of milliseconds that a user can be inactive
	 */
	public static final long INACTIVITY_LIMIT = 9000l;

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
	public static final short MAXHOPS = 5;

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
}
