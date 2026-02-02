package uno.model.utils.api;

/**
 * Interface for the game logging system.
 * It defines how game events, actions, and errors are recorded for auditing or replay purposes.
 */
public interface GameLogger {

    /**
     * Records a specific action performed by a player during the game.
     * @param playerName  The name of the player performing the action.
     * @param actionType  The type of action (e.g., "PLAY_CARD", "DRAW", "UNO_CALL").
     * @param cardDetails Specifics about the card involved (e.g., "RED_FIVE"), or "N/A".
     * @param extraInfo   Any additional context (e.g., "Target: Player2").
     */
    void logAction(String playerName, String actionType, String cardDetails, String extraInfo);

    void logError(String context, Exception e);
}
