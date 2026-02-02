package uno.view.scenes.api;

import uno.controller.api.MenuObserver;

/**
 * Interface representing the Main Menu screen of the application.
 * It defines the mechanism to attach a controller (observer) to handle user inputs.
 */
public interface MenuScene {

    /**
     * Registers an observer to listen for menu events (e.g., button clicks).
     *
     * @param observer The controller implementing MenuObserver.
     */
    void setObserver(MenuObserver observer);
}
