package uno.view.scenes.api;

import uno.controller.api.MenuObserver;

/**
 * Interface representing the Rules Configuration screen (House Rules).
 * It allows the user to toggle specific game rules and allows the controller
 * to retrieve these settings before starting a game.
 */
public interface RulesScene {

    /**
     * Registers the observer (controller) to handle navigation events.
     * 
     * @param observer The controller instance.
     */
    void setObserver(MenuObserver observer);

    /**
     * Checks if the "UNO Penalty" rule is enabled.
     * 
     * @return true if the penalty is active, false otherwise.
     */
    boolean isUnoPenaltyEnabled();

    /**
     * Checks if the "Skip Turn After Draw" rule is enabled.
     * 
     * @return true if players must skip their turn after drawing a card.
     */
    boolean isSkipAfterDrawEnabled();

    /**
     * Checks if the "Mandatory Pass" rule is enabled.
     * 
     * @return true if the game ends/passes when the deck is empty (no reshuffle).
     */
    boolean isMandatoryPassEnabled();

    /**
     * Checks if the "Scoring Mode" rule is enabled.
     * 
     * @return true if the game ends when a player reaches a score limit.
     */
    boolean isScoringModeEnabled();
}
