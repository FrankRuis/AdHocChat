package dataobjects;

import java.awt.*;
import java.io.Serializable;

/**
 * Chat message object, containing information about the sender, destination and message
 * 
 * @author Frank
 */
public class ChatMessage implements Serializable {
	
	private static final long serialVersionUID = 5734199577340633482L;

	private User user;
	
	private Color color;
	private int fontSize;
	private String font;
	private boolean bold;
	private boolean italic;
	
	private String destination;
	private String message;
	
	/**
	 * Constructor with assignments
	 * @param user The user that sent the chat message
	 * @param color The text color
	 * @param fontSize The font size
	 * @param font The font name
	 * @param bold Whether or not the text should be bolded
	 * @param italic Whether or not the text should be italicized
	 * @param message The message text
	 * @param destination The destination tab name
	 */
	public ChatMessage(User user, Color color, int fontSize, String font, boolean bold, boolean italic, String message, String destination) {
		this.user = user;
		this.color = color;
		this.fontSize = fontSize;
		this.font = font;
		this.bold = bold;
		this.italic = italic;
		this.message = message;
		this.destination = destination;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return the fontSize
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * @return the font
	 */
	public String getFont() {
		return font;
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(String font) {
		this.font = font;
	}

	/**
	 * @return the bold
	 */
	public boolean isBold() {
		return bold;
	}

	/**
	 * @param bold the bold to set
	 */
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	/**
	 * @return the italic
	 */
	public boolean isItalic() {
		return italic;
	}

	/**
	 * @param italic the italic to set
	 */
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ChatMessage [user=" + user + ", color=" + color + ", fontSize="
				+ fontSize + ", font=" + font + ", bold=" + bold + ", italic="
				+ italic + ", destination=" + destination + ", message="
				+ message + "]";
	}
}
