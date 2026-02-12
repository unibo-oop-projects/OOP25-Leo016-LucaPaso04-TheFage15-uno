package uno.model.game.api;

import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;

import java.util.List;

/**
 * Factory interface for creating Game instances.
 * Encapsulates the complexity of setting up decks, players, and rules.
 */
public interface GameFactory {

    /**
     * Creates a new Game instance.
     * 
     * @param playerName The name of the human player.
     * @param gameMode   The selected game mode (e.g., "Classica", "Uno
     *                   Flip").
     * @param players    The list of players participating in the game.
     * @return A fully initialized Game instance.
     */
    Game createGame(String playerName, GameMode gameMode, List<AbstractPlayer> players);

    /**
     * Return the logger.
     * 
     * @return The GameLogger instance used for logging game events.
     */
    GameLogger getLogger();
}
