package uno.model.cards.behaviors.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.game.api.Game;

/**
 * Implementation of {@link CardSideBehavior} for the "Flip" card.
 * This behavior triggers the central mechanic of UNO Flip: inverting the entire game state
 * from the Light Side to the Dark Side (or vice versa). This affects the draw deck,
 * the discard pile, and the hands of all players.
 */
public class FlipBehavior implements CardSideBehavior {

    private final CardColor color;
    private final CardValue value;

    /**
     * Constructs a Flip behavior.
     * @param color The specific color of the Flip card (e.g., RED, TEAL).
     * @param value The value, typically {@code FLIP}.
     */
    public FlipBehavior(final CardColor color, final CardValue value) {
        this.color = color;
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeEffect(final Game game) {
        game.flipTheWorld();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardColor getColor() {
        return color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardValue getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s %s (SWITCH SIDE)", color, value);
    }
}
