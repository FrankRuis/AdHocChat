package gui;

import dataobjects.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author frank
 */
public class UserDialogWindow implements ActionListener {

    private JFrame frame;
    private JButton privChatButton;

    private MainGUI gui;
    private User user;

    /**
     * Initialize the login screen
     */
    public UserDialogWindow(MainGUI gui, User user) {
        this.gui = gui;
        this.user = user;

        try {
            // Try to get the system's look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Keep the standard look and feel if an error occurs
        }

        // Create the frame
        frame = new JFrame();
        frame.setTitle(user.getName());
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage("Images/online.png"));
        frame.setMinimumSize(new Dimension(200,100));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(gui.getTabPane());

        // Create the main panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 1));

        // Create the button panel
        JPanel buttonPanel = new JPanel();
        privChatButton = new JButton("Start private chat");
        privChatButton.setFocusable(false);
        privChatButton.addActionListener(this);
        buttonPanel.add(privChatButton);
        mainPanel.add(buttonPanel);

        frame.getContentPane().add(mainPanel);

        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source.equals(privChatButton)) {
            gui.startPrivateChat(user);
            frame.dispose();
        }
    }

    /**
     * Dispose this window
     */
    public void dispose() {
        frame.dispose();
    }
}
