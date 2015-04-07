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
}
