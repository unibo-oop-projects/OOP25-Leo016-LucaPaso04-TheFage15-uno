package uno.model.game.api;

import uno.model.cards.types.api.Card;

import java.util.List;
import java.util.Optional;

/**
 * Interface defining the behavior of the Discard Pile.
 * The Discard Pile holds cards that have been played. It provides access to the
 * top-most card (active card) and allows recycling cards back into the Draw
 * Deck.
 */
public interface DiscardPile {

    /**
     * Places a card on top of the pile.
     * This becomes the new active card that players must match.
     * 
     * @param card The card to add. Must not be null.
     */
    void addCard(Card card);

    /**
     * Retrieves the card currently on top of the pile without removing it.
     * 
     * @return An {@link Optional} containing the top card, or {@code empty} if the
     *         pile is empty.
     */
    Optional<Card> getTopCard();

    /**
     * Extracts all cards from the pile.
     * This is used when starting a new round to recycle the cards.
     * 
     * @return A list of cards to be recycled.
     */
    List<Card> takeAll();

    /**
     * Extracts all cards from the pile EXCEPT the top one.
     * This is used when the Draw Deck is empty. The pile is cleared (leaving only
     * the top card),
     * and the extracted cards are returned to be shuffled into the new Draw Deck.
     * 
     * @return A list of cards to be recycled.
     */
    List<Card> takeAllExceptTop();

    /**
     * Returns a safe copy of all cards currently in the pile.
     * Useful for the View/GUI to render the stack or for debug logging.
     * 
     * @return A list of all cards.
     */
    List<Card> getSnapshot();

    /**
     * Checks if the pile is empty.
     * 
     * @return true if no cards are in the pile.
     */
    boolean isEmpty();

    /**
     * Returns the current number of cards in the pile.
     * 
     * @return The size of the pile.
     */
    int size();
}
