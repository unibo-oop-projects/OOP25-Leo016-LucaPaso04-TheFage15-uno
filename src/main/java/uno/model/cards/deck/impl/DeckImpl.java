package uno.model.cards.deck.impl;

import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Abstract implementation of the {@link Deck} interface.
 * This class handles the underlying storage (ArrayList) and standard operations
 * like shuffling and drawing, while delegating the specific deck composition
 * to the concrete subclasses (e.g., UnoDeck).
 * @param <T> The type of Card this deck contains.
 */
public abstract class DeckImpl<T extends Card> implements Deck<T> {

    // Changed from protected to private for better encapsulation.
    // Subclasses should use addCard() or refill() to modify contents.
    private final List<T> cards;

    /**
     * Default constructor. Initializes an empty deck.
     * Concrete subclasses must call {@link #refill(List)} or {@link #addCard(Card)}
     * to populate it.
     */
    public DeckImpl() {
        this.cards = new ArrayList<>();
    }

    /**
     * Constructor that accepts an initial set of cards.
     * Useful for testing or creating a deck from a known state.
     * @param initialCards The cards to start with.
     */
    public DeckImpl(final List<T> initialCards) {
        this.cards = new ArrayList<>(initialCards);
        shuffle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shuffle() {
        if (!cards.isEmpty()) {
            Collections.shuffle(cards);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<T> draw() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        // Remove from the end (top) of the list for O(1) performance
        return Optional.of(cards.remove(cards.size() - 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<T> peek() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cards.get(cards.size() - 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCard(final T card) {
        if (card != null) {
            cards.add(card);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refill(final List<T> newCards) {
        if (newCards != null && !newCards.isEmpty()) {
            this.cards.addAll(newCards);
        }
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
    public String toString() {
        return "DeckImpl{size=" + size() + "}";
    }
}
