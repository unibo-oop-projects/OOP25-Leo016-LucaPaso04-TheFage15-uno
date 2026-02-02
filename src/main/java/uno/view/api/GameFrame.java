package uno.view.api;

import javax.swing.JPanel;

/**
 * Interface representing the main application window.
 * It acts as a container that can switch between different views (scenes)
 * like the Menu, the Game Board, or the Rules screen.
 */
public interface GameFrame {

    /**
     * Replaces the currently displayed content with a new scene.
     *
     * @param scene The JPanel representing the new scene to display.
     */
    void showScene(JPanel scene);

    /**
     * Controls the visibility of the main window.
     * Use this to show the window after initialization.
     *
     * @param visible true to make the window visible, false to hide it.
     */
    void setVisible(boolean visible);

    /**
     * Releases all of the native screen resources used by this Window,
     * its subcomponents, and all of its owned children.
     */
    void dispose();
}
