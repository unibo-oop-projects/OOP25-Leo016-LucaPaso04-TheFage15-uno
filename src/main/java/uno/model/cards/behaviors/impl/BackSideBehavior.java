package uno.model.cards.behaviors.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.game.api.Game;

/**
 * Represents the back side (or inactive side) of a card.
 * This class implements the Null Object/Singleton pattern.
 * Since the back of a card has no color, value, or effect, attempting to access
 * these properties indicates a logic error in the game flow.
 */
public final class BackSideBehavior implements CardSideBehavior {

    // Singleton Instance: We only need one instance for the whole application.
    private static final BackSideBehavior INSTANCE = new BackSideBehavior();

    // Private constructor to prevent instantiation from outside
    private BackSideBehavior() {

    }

    /**
     * Retrieves the singleton instance of the card back behavior.
     * @return The shared instance.
     */
    public static BackSideBehavior getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardColor getColor() {
        throw new UnsupportedOperationException("Critical Error: Attempted to get Color from the Card Back.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardValue getValue() {
        throw new UnsupportedOperationException("Critical Error: Attempted to get Value from the Card Back.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeEffect(final Game game) {
        throw new IllegalStateException("Critical Error: Attempted to play the Card Back as an action.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CARD_BACK";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWild() {
        return false;
    }
}
