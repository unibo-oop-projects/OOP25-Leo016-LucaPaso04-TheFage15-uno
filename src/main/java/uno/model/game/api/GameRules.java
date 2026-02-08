package uno.model.game.api;

/**
 * Interface representing the customizable rules for a Game session.
 */
public interface GameRules {

    /**
     * @return true if the UNO penalty rule is active.
     */
    boolean isUnoPenaltyEnabled();

    /**
     * @return true if playing immediately after drawing is forbidden.
     */
    boolean isSkipAfterDrawEnabled();

    /**
     * @return true if the discard pile should NOT be reshuffled when deck is empty.
     */
    boolean isMandatoryPassEnabled();

    /**
     * @return true if the game ends when a player reaches a score limit (e.g. 500),
     *         false if it ends after a single round.
     */
    boolean isScoringModeEnabled();
}
