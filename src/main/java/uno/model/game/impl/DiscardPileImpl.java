package uno.model.game.impl;

import uno.model.cards.types.api.Card;
import uno.model.game.api.DiscardPile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of the Discard Pile using an ArrayList.
 */
public class DiscardPileImpl implements DiscardPile {

    // Using ArrayList acts as a Stack (Last-In-First-Out logic for the top card)
    private final List<Card> cards;

    /**
     * Constructor initializing an empty discard pile.
     */
    public DiscardPileImpl() {
        this.cards = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCard(final Card card) {
        if (Optional.ofNullable(card).isEmpty()) {
            throw new IllegalArgumentException("Cannot add a null card to the discard pile.");
        }
        this.cards.add(card);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Card> getTopCard() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        // The last element in the list represents the physical top of the pile
        return Optional.of(cards.get(cards.size() - 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Card> takeAllExceptTop() {
        if (cards.size() <= 1) {
            return new ArrayList<>();
        }

        // 1. Identify the top card (to keep)
        final Card topCard = cards.get(cards.size() - 1);

        // 2. Extract all other cards (0 to size-1) for reshuffling
        // We create a new list copy to return
        final List<Card> cardsToRecycle = new ArrayList<>(cards.subList(0, cards.size() - 1));

        // 3. Clear internal state and restore top card
        cards.clear();
        cards.add(topCard);

        return cardsToRecycle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Card> getSnapshot() {
        // Defensive copy to prevent external modification of the internal list
        return new ArrayList<>(this.cards);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return cards.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reverse() {
        // Essential for UNO Flip mechanics
        Collections.reverse(cards);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DiscardPile{size=" + size() + ", top=" + getTopCard().orElse(null) + "}";
    }
}
