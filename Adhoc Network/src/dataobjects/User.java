package dataobjects;

import java.awt.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Random;

/**
 * Contains information about the user connected to the chat
 * 
 * @author Frank
 */
public class User implements Serializable {

	private static final long serialVersionUID = -4255785705635377381L;
	
	private String name;
	private Color color;
	private Color textColor;

	private int address;

	private Timestamp lastSeen;
	
	/**
	 * Constructor with assignments
	 * @param name The username
	 * @param color The color for the username, random color is chosen if null
	 */
	public User(String name, Color color) {
		this.lastSeen = new Timestamp(System.currentTimeMillis());

		if (name != null && !name.trim().equals("")) {
			this.name = name;
		} else {
			this.name = "Anonymous";
		}
		
		// Set the color or choose a random bright color if the given color is null
		Random random = new Random();
		float hue = random.nextFloat();
		float saturation = 1f;
		float brightness = 0.85f;
		
		this.color = color != null ? color : Color.getHSBColor(hue, saturation, brightness);
		this.textColor = Color.BLACK;
	}

	/**
	 * Set the last seen timestamp to the current time
	 */
	public void setLastSeen() {
		this.lastSeen = new Timestamp(System.currentTimeMillis());
	}

	/**
	 * Get the last seen timestamp
	 * @return The timestamp
	 */
	public Timestamp getLastSeen() {
		return this.lastSeen;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the text color
	 */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * @param color the text color to set
	 */
	public void setTextColor(Color color) {
		this.textColor = color;
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
	 * @return the address
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(int address) {
		this.address = address;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [name=" + name + ", color=" + color + ", address=" + address + "]";
	}
}
