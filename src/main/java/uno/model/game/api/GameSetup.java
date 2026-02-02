package uno.model.game.api;

/**
 * Interface responsible for the initialization phase of an UNO game.
 * It handles the initial distribution of cards to players and the determination
 * of the starting card on the discard pile, applying specific rules based on the game mode.
 */
public interface GameSetup {

    /**
     * Executes the complete setup sequence:
     * Shuffles the deck.
     * Deals the initial hand (7 cards) to all players.
     * Draws and validates the first card for the discard pile.
     *
     * @param isAllWild {@code true} if the game is in "All Wild" mode (affects starting card rules).
     */
    void initializeGame(boolean isAllWild);
}
