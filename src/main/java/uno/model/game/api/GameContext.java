package uno.model.game.api;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;

import java.util.Optional;

/**
 * Interface that exposes the internal context of the Game to the State
 * implementations.
 * This allows states to be decoupled from the concrete GameImpl class.
 */
public interface GameContext extends Game {

    /**
     * Sets the current state of the game.
     * 
     * @param newState The new state behavior.
     */
    void setGameState(GameStateBehavior newState);

    /**
     * Gets the game logger.
     * 
     * @return The logger instance.
     */
    GameLogger getLogger();

    /**
     * Checks if a move is valid.
     * 
     * @param card The card to check.
     * @return true if valid, false otherwise.
     */
    boolean isValidMove(Card card);

    /**
     * Checks if a player has any playable card.
     * 
     * @param player The player to check.
     * @return true if has playable card, false otherwise.
     */
    boolean playerHasPlayableCard(AbstractPlayer player);

    /**
     * Sets the currently played card (the one causing effects).
     * 
     * @param card The card.
     */
    void setCurrentPlayedCard(Card card);

    /**
     * Gets the currently played card.
     * 
     * @return The card.
     */
    Card getCurrentPlayedCard();

    /**
     * Sets the current color optional.
     * 
     * @param color The optional color.
     */
    void setCurrentColorOptional(Optional<CardColor> color);

    /**
     * Sets the winner of the game.
     * 
     * @param winner The winner.
     */
    void setWinner(AbstractPlayer winner);

    /**
     * {@inheritDoc}
     */
    @Override
    void startNewRound();
}
