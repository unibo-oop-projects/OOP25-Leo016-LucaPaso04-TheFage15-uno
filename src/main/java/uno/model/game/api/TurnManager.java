package uno.model.game.api;

import uno.model.players.impl.AbstractPlayer;

/**
 * Interface for managing the flow of turns in the UNO game.
 * It handles the cyclic order of players, direction
 * (clockwise/counter-clockwise),
 * skipping logic, and tracks turn-specific states (e.g., if the current player
 * has drawn).
 */
public interface TurnManager {

    /**
     * Retrieves the player whose turn it is currently.
     * 
     * @return The active {@link AbstractPlayer}.
     */
    AbstractPlayer getCurrentPlayer();

    /**
     * Advances the game state to the next player.
     * This calculates the next index based on the current direction and skip value,
     * resets turn-specific flags, and performs start-of-turn checks (like UNO
     * penalty).
     * 
     * @param game The current game instance (needed for penalty application).
     */
    void advanceTurn(Game game);

    /**
     * Previews who the next player will be without changing the state.
     * Useful for UI hints or for "Targeted Draw" cards to know who receives the
     * cards.
     * 
     * @return The next {@link AbstractPlayer} in line.
     */
    AbstractPlayer peekNextPlayer();

    /**
     * Toggles the direction of play (Clockwise <-> Counter-Clockwise).
     */
    void reverseDirection();

    /**
     * Sets the number of players to skip in the next turn advancement.
     * 
     * @param n Number of players to skip (e.g., 1 for a Skip card).
     */
    void skipPlayers(int n);

    /**
     * Checks if the current player has already drawn a card this turn.
     * 
     * @return true if the action "Draw" has been performed.
     */
    boolean hasDrawnThisTurn();

    /**
     * Updates the draw status for the current turn.
     * 
     * @param value true if the player has drawn.
     */
    void setHasDrawnThisTurn(boolean value);

    /**
     * Checks the current direction of the game.
     * 
     * @return true for Clockwise, false for Counter-Clockwise.
     */
    boolean isClockwise();

    /**
     * Resets the turn manager state for a new round.
     * This involves resetting the direction, clearing flags,
     * and potentially randomizing the starting player again.
     */
    void reset();
}
