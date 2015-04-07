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
		String value = (String) attributeSet.getAttribute(tag);
		
		// Check the element contained the clickable attribute
		if (value != null) {
			// TODO start a private chat
			// If a username other than the current user is clicked
			if (!value.equals(gui.getCurrentUser().getName())) {
				// Add a tab with the clicked username as the title
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
