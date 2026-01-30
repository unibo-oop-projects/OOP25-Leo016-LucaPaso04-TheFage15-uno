package uno.controller.api;

/**
 * Interface for the Main Menu Controller.
 * It handles the user interactions within the main menu, such as selecting game modes,
 * opening rules, or quitting the application.
 */
public interface MenuController extends MenuObserver {

    /**
     * Starts a new game in "Classic" mode.
     * Initializes the standard deck and standard AI opponents.
     */
    @Override
    void onStartClassicGame();

    /**
     * Starts a new game in "Flip" mode.
     * Initializes the Flip deck (double-sided cards) and Flip AI opponents.
     */
    @Override
    void onStartFlipGame();

    /**
     * Starts a new game in "All Wild" mode.
     * Initializes the All Wild deck and specialized AI opponents.
     */
    @Override
    void onStartAllWildGame();

    /**
     * Opens the rules screen to display game instructions.
     */
    @Override
    void onOpenRules();

    /**
     * Terminates the application.
     */
    @Override
    void onQuit();
}
