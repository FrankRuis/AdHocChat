package utils;

import gui.MainGUI;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Mouse listener that checks if a clicked element in a JTextPane has the clickable attribute
 * 
 * @author Frank
 */
public class ClickableListener implements MouseListener {

	private JTextPane textPane;
	private MainGUI gui;
	private String tag = "clickable";

	/**
	 * Constructor
	 * @param textPane The JTextPane to listen in
	 */
	public ClickableListener(JTextPane textPane, MainGUI gui) {
		this.textPane = textPane;
		this.gui = gui;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// Get the clicked element
		Element element = textPane.getStyledDocument().getCharacterElement(textPane.viewToModel(e.getPoint()));
		
		// Get the attribute set of the clicked element
		AttributeSet attributeSet = element.getAttributes();
		int value = attributeSet.getAttribute(tag) != null ? Integer.parseInt((String) attributeSet.getAttribute(tag)) : -1;

		// Check the element contained the clickable attribute
		if (value > 0) {
			// If the value is not equal to our address
			if (value != Protocol.SOURCE) {
				// Add a tab for a private chat with the clicked user
				gui.startPrivateChat(value);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

}
