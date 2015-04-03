package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Custom mouse listener for JTabbedPane
 *
 * @author Frank
 */
public class TabMouseListener implements MouseListener {

    JTabbedPane pane;

    public TabMouseListener(JTabbedPane pane) {
        this.pane = pane;
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        int tab = pane.getUI().tabForCoordinate(pane, e.getX(), e.getY());

        // If the right mouse button was pressed
        if(e.getButton() == MouseEvent.BUTTON3){
            // Don't allow the main tab to be removed
            if (!pane.getTitleAt(tab).equals("Chatroom")) {
                // Remove the tab
                pane.removeTabAt(tab);
            } else {
                // Set the foreground color to black
                pane.setForegroundAt(tab, Color.black);
            }

        // If a different mouse button was pressed
        } else {
            // Set the foreground color to black
            pane.setForegroundAt(tab, Color.black);
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {

    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e
     */
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e
     */
    @Override
    public void mouseExited(MouseEvent e) {

    }
}
