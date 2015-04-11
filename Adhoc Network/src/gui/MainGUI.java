package gui;

import client.Client;
import dataobjects.ChatMessage;
import dataobjects.User;
import utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main graphical user interface for the ad hoc chatroom
 * 
 * @author Frank
 */
public class MainGUI implements ActionListener, Observer {

	private final int MAX_CHARS = 5000; // Maximum amount of characters to display in a single JTextPane
	
	private JFrame frame;

	private JTabbedPane tabPanel;
	private Map<String, JTextPane> chatPanes;
	private Map<String, JScrollPane> scrollPanes;

	private JMenu mOptions;

	private JMenuItem miConnect;
	private JMenuItem miDisconnect;

	private JMenuItem miChangeNameColor;
	private JMenuItem miChangeName;
	private JMenuItem miChangeTextColor;
	
	private JTextField inputField;
	private TextFieldKeyListener inputFieldListener;
	
	private User currentUser;
	
	private Client client;
	
	// Whether or not timestamps should be displayed for messages and notifications
	private boolean useTimestamps = true;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGUI window = new MainGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Start a private chat with the given user
	 * @param user The user to start a private chat with
	 */
	public void startPrivateChat(User user) {
		if (user != null) {
			newTab(user.getName());
			client.addDestination(user.getName(), user.getAddress());
			client.sendMessage(Protocol.PRIVCHAT + " " + currentUser.getName(), user.getName());
		} else {
			showNotification("There is no open connection with that user.", Protocol.MAINCHAT);
		}
	}

	/**
	 * Add a new tab to the JTabbedPane
	 * @param name The title of the tab
	 */
	public void newTab(String name) {
		// If a tab with this name does not yet exist
		if (!chatPanes.containsKey(name)) {
			// Create a JTextPane for the chat messages
			final JTextPane chatPane = new JTextPane();
			chatPane.setContentType("text/plain; charset=UTF-8");
			chatPane.setEditable(false);
			
			// Add a mouse listener to allow certain elements in the chat pane to be clickable
			chatPane.addMouseListener(new ClickableListener(chatPane, this));
			
			// Add a document listener to check for emoticons
			chatPane.getStyledDocument().addDocumentListener(new EmoticonListener());
			
			// Create a JScrollPane for the JTextPane
			JScrollPane chatScrollPane = new JScrollPane();
			chatScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			chatScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			chatScrollPane.setViewportView(chatPane);
			
			// Add the chat pane to the map with its name as key for future references
			chatPanes.put(name, chatPane);
			scrollPanes.put(name, chatScrollPane);

			// Add the JScrollPane to a new tab
			tabPanel.addTab(name, null, chatScrollPane, null);
		}
	}

	/**
	 * Remove the tab with the given index
	 * @param i The tab index
	 */
	public void removeTab(int i) {
		String title = tabPanel.getTitleAt(i);
		tabPanel.removeTabAt(i);
		chatPanes.remove(title);
		scrollPanes.remove(title);
	}
	
	/**
	 * Return an AttributeSet with the given values
	 * @param color The color
	 * @param font The font name
	 * @param size The font size
	 * @param bold Whether or not the text should be bolded
	 * @param italic Whether or not the text should be italicized
	 * @return The attribute set
	 */
	public AttributeSet getTextStyle(Color color, String font, int size, boolean bold, boolean italic) {
		StyleContext styleContext = StyleContext.getDefaultStyleContext();
		AttributeSet attributeSet = styleContext.addAttribute(SimpleAttributeSet.EMPTY,StyleConstants.Foreground, color);
		attributeSet = styleContext.addAttribute(attributeSet, StyleConstants.FontFamily, font);
		attributeSet = styleContext.addAttribute(attributeSet, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
		attributeSet = styleContext.addAttribute(attributeSet, StyleConstants.FontSize, size);
		attributeSet = styleContext.addAttribute(attributeSet, StyleConstants.Bold, bold);
		attributeSet = styleContext.addAttribute(attributeSet, StyleConstants.Italic, italic);
		
		// Return the created attribute set
		return attributeSet;
	}
	
	/**
	 * Give a given attribute set the clickable attribute with a given value
	 * @param aSet The old attribute set
	 * @param value The value for the clickable attribute
	 * @return The new attribute set
	 */
	public AttributeSet makeClickable(AttributeSet aSet, String value) {
		return StyleContext.getDefaultStyleContext().addAttribute(aSet, "clickable", value);
	}
	
	/**
	 * Append a message to a chat pane
	 * @param message A ChatMessage object
	 */
	public void append(ChatMessage message) {
		// Get the destination chat pane
		String destination = (chatPanes.get(message.getDestination()) != null) ? message.getDestination() : message.getUser().getName();

		// If a chatpane for the destination exists
		if (chatPanes.get(destination) != null) {
			// Get the styled document of the chat pane given by the ChatMessage object
			StyledDocument doc = chatPanes.get(destination).getStyledDocument();

			// Create the attribute set for the username and make the username clickable
			AttributeSet unameAset = getTextStyle(message.getUser().getColor(), "Tahoma", 12, true, false);
			unameAset = makeClickable(unameAset, "" + message.getUser().getAddress());

			// Create an attribute set with the parameters given by the ChatMessage object
			AttributeSet attributeSet = getTextStyle(message.getColor(), message.getFont(), message.getFontSize(), message.isBold(), message.isItalic());

			// Create an attribute set for the timestamps
			AttributeSet timeAset = getTextStyle(Color.gray, "Calibri", 12, false, false);

			// Create a timestamp
			SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm]");
			String time = sdf.format(new Date());

			// Insert the chat message into the document
			try {
				if (useTimestamps) doc.insertString(doc.getLength(), time + " ", timeAset);
				doc.insertString(doc.getLength(), message.getUser().getName() + ": ", unameAset);
				doc.insertString(doc.getLength(), message.getMessage() + "\n", attributeSet);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			// Remove first line if the character limit has been reached
			if (doc.getLength() > MAX_CHARS) {
				Element firstLine = doc.getDefaultRootElement().getElement(0);
				try {
					doc.remove(firstLine.getStartOffset(), firstLine.getEndOffset());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

			// Make sure the scroll bar is set to the end of the text panel
			chatPanes.get(destination).setCaretPosition(doc.getLength());

			// If the message was added to a background tab
			if (!destination.equals(getActiveTab())) {
				tabPanel.setForegroundAt(tabPanel.indexOfComponent(scrollPanes.get(destination)), Color.red);
			}
		}
	}
	
	/**
	 * Print a notification with timestamp
	 * @param notification The text for the notification
	 * @param destination The destination of the notification
	 */
	public void showNotification(String notification, String destination) {
		// If a chatpane for the destination exists
		if (chatPanes.get(destination) != null) {
			// Get the styled document of the destination
			StyledDocument doc = chatPanes.get(destination).getStyledDocument();

			// Create the attributesets for the timestamp and the notification
			AttributeSet timeAset = getTextStyle(Color.gray, "Calibri", 12, false, false);
			AttributeSet attributeSet = getTextStyle(Color.gray, "Calibri", 16, false, true);

			// Create a timestamp for the notification
			SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm]");
			String time = sdf.format(new Date());

			// Insert the notification into the document
			try {
				if (useTimestamps) doc.insertString(doc.getLength(), time + " ", timeAset);
				doc.insertString(doc.getLength(), notification + "\n", attributeSet);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			// Remove first line if the character limit has been reached
			if (doc.getLength() > MAX_CHARS) {
				Element firstLine = doc.getDefaultRootElement().getElement(0);
				try {
					doc.remove(firstLine.getStartOffset(), firstLine.getEndOffset());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

			// Make sure the scroll bar is set to the end of the text panel
			chatPanes.get(destination).setCaretPosition(doc.getLength());

			// If the message was added to a background tab
			if (!destination.equals(getActiveTab())) {
				tabPanel.setForegroundAt(tabPanel.indexOfComponent(scrollPanes.get(destination)), Color.red);
			}
		}
	}
	
	/**
	 * @return The title of the active tab
	 */
	public String getActiveTab() {
		return tabPanel.getTitleAt(tabPanel.getSelectedIndex());
	}

	/**
	 * @return The user object for the user using the chat
	 */
	public User getCurrentUser() {
		return currentUser;
	}

	/**
	 * Get the user with the given address
	 * @param address The address of the user to get
	 * @return The user object
	 */
	public User getUser(int address) {
		return client.getUser(address);
	}

	/**
	 * Called when a menu item is pressed
	 * @param e The action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Save which item called the event
		Object source = e.getSource();
		
		// If the connect menu item was pressed
		if (source.equals(miConnect)) {
			if (client == null) {
				showNotification("Connecting...", Protocol.MAINCHAT);
				
				// Ask the user to enter a username
				String username = (String) JOptionPane.showInputDialog(frame, "Enter your desired username:\n", "Username selection", JOptionPane.PLAIN_MESSAGE, null, null, "");
				currentUser = new User(username.trim(), null);
				
				// Set the user's address
				currentUser.setAddress(Protocol.getSourceAddress());
				
				// Create the client and add the GUI as an observer
				client = new Client("228.0.0.4", 1231);
				client.addObserver(this);
				
				// Create a KeyListener for the textfield
				inputFieldListener = new TextFieldKeyListener(this, client);
				inputField.addKeyListener(inputFieldListener);
				
				// Add the user to the client's list of connected users
				client.addUser(currentUser);
				
				// Start the client thread
				Thread t = new Thread(client);
				t.start();

				mOptions.setEnabled(true);
			} else {
				showNotification("You are already connected.", Protocol.MAINCHAT);
			}
		}
		
		// If the disconnect menu item was pressed
		if (source.equals(this.miDisconnect)) {
			if (client != null) {
				inputField.removeKeyListener(inputFieldListener);
				inputFieldListener = null;
				client.disconnect();
				client = null;

				// Remove all tabs except the main tab
				for (int i = 1; i < tabPanel.getTabCount(); i++) {
					chatPanes.remove(tabPanel.getTitleAt(i));
					scrollPanes.remove(tabPanel.getTitleAt(i));
					tabPanel.removeTabAt(i);
				}

				mOptions.setEnabled(false);

				showNotification("Disconnected.", Protocol.MAINCHAT);
			} else {
				showNotification("You are not connected.", Protocol.MAINCHAT);
			}
		}

		// If the change name color menu item was pressed
		if (source.equals(miChangeNameColor)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// Create a custom color chooser panel with only the HSV and preview panels
					JColorChooser colorChooser = new JColorChooser();
					AbstractColorChooserPanel[] chooserPanels = colorChooser.getChooserPanels();

					JPanel hsvPanel = new JPanel();
					chooserPanels[1].setBorder(new TitledBorder(chooserPanels[1].getDisplayName()));
					hsvPanel.add(chooserPanels[1]);

					JPanel colorChooserPanel = new JPanel(new BorderLayout(1,1));
					colorChooserPanel.add(hsvPanel, BorderLayout.CENTER);

					colorChooserPanel.add(colorChooser.getPreviewPanel(), BorderLayout.SOUTH);

					JOptionPane.showMessageDialog(null, colorChooserPanel, "Choose a color", JOptionPane.PLAIN_MESSAGE);

					currentUser.setColor(colorChooser.getColor());
				}
			});
		}

		// If the change text color menu item was pressed
		if (source.equals(miChangeTextColor)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// Create a custom color chooser panel with only the HSV and preview panels
					JColorChooser colorChooser = new JColorChooser();
					AbstractColorChooserPanel[] chooserPanels = colorChooser.getChooserPanels();

					JPanel hsvPanel = new JPanel();
					chooserPanels[1].setBorder(new TitledBorder(chooserPanels[1].getDisplayName()));
					hsvPanel.add(chooserPanels[1]);

					JPanel colorChooserPanel = new JPanel(new BorderLayout(1,1));
					colorChooserPanel.add(hsvPanel, BorderLayout.CENTER);

					colorChooserPanel.add(colorChooser.getPreviewPanel(), BorderLayout.SOUTH);

					JOptionPane.showMessageDialog(null, colorChooserPanel, "Choose a color", JOptionPane.PLAIN_MESSAGE);

					currentUser.setTextColor(colorChooser.getColor());
				}
			});
		}

		// If the change name menu item was pressed
		if (source.equals(miChangeName)) {
			// Ask the user to enter a username
			String username = (String) JOptionPane.showInputDialog(frame, "Enter your desired username:\n", "Username selection", JOptionPane.PLAIN_MESSAGE, null, null, "");

			// Don't change the username if nothing was entered
			if (username != null && !username.trim().isEmpty()){
				client.sendMessage(Protocol.NAME_CHANGE + " " + currentUser.getName() + " " + username, Protocol.MAINCHAT);
				currentUser.setName(username);
			}
		}
	}
	
	@Override
	public void update(Observable o, final Object arg) {
		try {
			// Wait until all events have been processed before updating the GUI
			SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    // If the argument is a ChatMessage object
                    if (arg.getClass().equals(ChatMessage.class)) {
                        append((ChatMessage) arg);
                    }

                    // If the argument is a String
                    if (arg.getClass().equals(String.class)) {
                        // Split the string on the first space
                        final String[] command = ((String) arg).split("\\s+", 2);

                        // Check the command type
                        switch (command[0]) {
                            case Protocol.PRIVCHAT:
                                newTab(command[1]);
                                break;
                            case Protocol.NOTIFY:
                                showNotification(command[1], Protocol.MAINCHAT);
                                break;
                            case Protocol.PART:
                                showNotification("User " + command[1] + " has left the chat.", Protocol.MAINCHAT);
                                break;
                            default:
								System.err.println("Unknown command received in GUI.");
								break;
                        }
                    }
                }});
		} catch (InterruptedException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the application.
	 */
	public MainGUI() {
		chatPanes = new HashMap<>();
		scrollPanes = new HashMap<>();
		
		// Initialize the GUI
		initialize();
		
		// Create the tab containing the main chat room
		newTab(Protocol.MAINCHAT);
	}

	/**
	 * Get the tab pane
	 * @return The JTabbedPane
	 */
	public JTabbedPane getTabPane() {
		return tabPanel;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
        try {
        	// Try to get the system's look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// Make sure the JTabbedPane has no borders
			UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
			UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			// Keep the standard look and feel when an error occurs
		}
        
        // Initialize the JFrame
		frame = new JFrame();
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage("Images/chat.png"));
		frame.setTitle("Chat");
		frame.setSize(500, 500);
		frame.setMinimumSize(new Dimension(300, 300));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		// Initialize the tabbed pane
		tabPanel = new JTabbedPane(JTabbedPane.TOP);
		tabPanel.setFocusable(false);
		tabPanel.addMouseListener(new TabMouseListener(this));
		frame.getContentPane().add(tabPanel, BorderLayout.CENTER);

		// Create and add the north panel
		JPanel northPanel = new JPanel();
		frame.getContentPane().add(northPanel, BorderLayout.NORTH);
		
		// Create and add the south panel
		JPanel southPanel = new JPanel();
		frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		// Create and add the input panel
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(0, 1, 0, 0));
		inputPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
		southPanel.add(inputPanel);
		
		// Create the input text field
		inputField = new JTextField();
		inputPanel.add(inputField);
		
		//Create the menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu mActions = new JMenu("Actions");
		mOptions = new JMenu("Options");

		mOptions.setEnabled(false);

		// Create the connection menu items
		miConnect = new JMenuItem("Connect");
		miConnect.addActionListener(this);
		miDisconnect = new JMenuItem("Disconnect");
		miDisconnect.addActionListener(this);
		
		mActions.add(miConnect);
		mActions.add(miDisconnect);

		// Create the options menu items
		miChangeName = new JMenuItem("Change name");
		miChangeName.addActionListener(this);
		miChangeNameColor = new JMenuItem("Change name color");
		miChangeNameColor.addActionListener(this);
		miChangeTextColor = new JMenuItem("Change text color");
		miChangeTextColor.addActionListener(this);

		mOptions.add(miChangeName);
		mOptions.add(miChangeNameColor);
		mOptions.add(miChangeTextColor);

		// Add the menus to the menu bar
		menuBar.add(mActions);
		menuBar.add(mOptions);
		frame.setJMenuBar(menuBar);
		
		// Create and add the west panel
		JPanel westPanel = new JPanel();
		frame.getContentPane().add(westPanel, BorderLayout.WEST);
		
		// Create and add the east panel
		JPanel eastPanel = new JPanel();
		frame.getContentPane().add(eastPanel, BorderLayout.EAST);
	}
}
