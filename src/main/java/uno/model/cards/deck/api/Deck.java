package uno.model.cards.deck.api;

import java.util.List;
import java.util.Optional;

import uno.model.cards.types.api.Card;

/**
 * Interface defining the contract for a generic deck of cards in the UNO game.
 * This interface abstracts the behavior of card collections, such as the 
 * Draw Pile (Deck) and the Discard Pile.
 * It uses {@link Optional} to safely handle cases where the deck is empty, 
 * eliminating the need for null checks or exception handling for game flow logic.
 * @param <T> The specific type of {@link Card} this deck holds.
 */
public interface Deck<T extends Card> {

    /**
     * Randomizes the order of the cards currently in the deck.
     * This should be called immediately after creation and whenever the 
     * discard pile is reshuffled into the draw deck.
     */
    void shuffle();

    /**
     * Removes and returns the card at the top of the deck.
     * @return An {@link Optional} containing the top card if available; 
     * {@code Optional.empty()} if the deck is exhausted.
     */
    Optional<T> draw();

    /**
     * Retrieves the card at the top of the deck without removing it.
     * This is essential for the Discard Pile to see the active card 
     * (matching color/value) or for AI to peek at the draw pile (if cheating/debugging).
     * @return An {@link Optional} containing the top card, or {@code empty} if the deck is empty.
     */
    Optional<T> peek();

    /**
     * Adds a single card to the deck.
     * For a Discard Pile, this places a played card on top.
     * For a Draw Pile, this is rarely used during play but useful during initialization.
     * @param card The card to be added. Must not be null.
     */
    void addCard(T card);

    /**
     * Replenishes the deck with a collection of new cards.
     * This is typically used when the Draw Pile is empty: the Discard Pile 
     * is shuffled and passed to this method to reform the Draw Pile.
     * @param newCards The list of cards to add to the deck.
     */
    void refill(List<T> newCards);

    /**
     * Checks if the deck contains no cards.
     * @return {@code true} if the deck is empty, {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * Returns the current number of cards in the deck.
     * @return The integer count of cards.
     */
    int size();
}
