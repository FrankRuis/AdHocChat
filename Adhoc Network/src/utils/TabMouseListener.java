package utils;

import gui.MainGUI;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Custom mouse listener for JTabbedPane
 *
 * @author Frank
 */
public class TabMouseListener implements MouseListener {

    MainGUI gui;

    public TabMouseListener(MainGUI gui) {
        this.gui = gui;
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        int tab = gui.getTabPane().getUI().tabForCoordinate(gui.getTabPane(), e.getX(), e.getY());

        // If the tab exists
        if (tab >= 0) {
            // If the right mouse button was pressed
            if (e.getButton() == MouseEvent.BUTTON3) {
                // Don't allow the main tab to be removed
                if (!gui.getTabPane().getTitleAt(tab).equals(Protocol.MAINCHAT)) {
                    // Remove the tab
                    gui.removeTab(tab);
                } else {
                    // Set the foreground color to black
                    gui.getTabPane().setForegroundAt(tab, Color.black);
                }

                // If a different mouse button was pressed
            } else {
                // Set the foreground color to black
                gui.getTabPane().setForegroundAt(tab, Color.black);
            }
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mousePressed(MouseEvent e) {

    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mouseExited(MouseEvent e) {

    }
}
