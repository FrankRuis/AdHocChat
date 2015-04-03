package utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Document listener that checks for emoticons and inserts them in the document that was changed
 * 
 * @author Frank
 */
public class EmoticonListener implements DocumentListener {

	private Map<String, ImageIcon> regexMap;
	
	/**
	 * Constructor
	 */
	public EmoticonListener() {
		regexMap = new HashMap<>();
		
		// Add emoticons and their regexes to the regexMap
		try {
			regexMap.put("[K|k]appa", new ImageIcon(ImageIO.read(new File("Images/kappa.png"))));
			regexMap.put(":[D|d]", new ImageIcon(ImageIO.read(new File("Images/smileyD.png"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {	
	}

	/**
	 * Insert emoticons in users' messages if applicable
	 */
	@Override
	public void insertUpdate(final DocumentEvent e) {
		// SwingUtilities.invokeLater so we don't try to change the document before the lock is released 
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Get the document that changed
				StyledDocument doc = (StyledDocument) e.getDocument();
				
				// Get the text that was inserted
				String insertion = null;
				
				try {
					insertion = doc.getText(e.getOffset(), e.getLength());
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
				
				// Go through all possible emoticons
				for (String regex : regexMap.keySet()) {
					// Get the index of the emoticon in the insertion
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(insertion);

					
					try {
						// If the text contained the regex of the emoticon
						while (matcher.find()) {
							// The index of the emoticon in the insertion
							int i = matcher.start();

							// Make an attributeset with the attributes of the regex
							SimpleAttributeSet attributeSet = new SimpleAttributeSet(doc.getCharacterElement(e.getOffset() + i).getAttributes());
							
							// Check if there is no icon yet
							if (StyleConstants.getIcon(attributeSet) == null) {
			
								// Set the icon to the corresponding regex
								StyleConstants.setIcon(attributeSet, regexMap.get(regex));
			
								// Remove the regex string and insert the icon
								doc.remove(e.getOffset() + i, matcher.group().length());
								doc.insertString(e.getOffset() + i, matcher.group(), attributeSet);
							}
						}
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}
}
