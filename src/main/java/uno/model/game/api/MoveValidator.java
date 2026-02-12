package uno.model.game.api;

import uno.model.cards.types.api.Card;
import uno.model.players.impl.AbstractPlayer;

/**
 * Interface responsible for validating moves in the game.
 */
public interface MoveValidator {

    /**
     * Checks if a card can be played.
     * 
     * @param cardToPlay The card to check.
     * @return true if valid, false otherwise.
     */
    boolean isValidMove(Card cardToPlay);

    /**
     * Checks if a player has any playable card.
     * 
     * @param player The player to check.
     * @return true if has playable card, false otherwise.
     */
    boolean playerHasPlayableCard(AbstractPlayer player);
}
