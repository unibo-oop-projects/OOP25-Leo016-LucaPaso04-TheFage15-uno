package uno.model.cards.behaviors.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.game.api.Game;

/**
 * Implementation of {@link CardSideBehavior} for standard numeric cards (0-9).
 * These cards represent the "vanilla" behavior in UNO: they have a color and a number,
 * but executing them triggers no special game state changes (like skipping or drawing).
 * The flow simply proceeds to the next player automatically handled by the Game engine.
 */
public class NumericBehavior implements CardSideBehavior {

    private final CardColor color;
    private final CardValue value;

    /**
     * Constructs a standard numeric card behavior.
     * @param color The card color (e.g., RED, BLUE).
     * @param value The numeric value (ZERO to NINE).
     */
    public NumericBehavior(final CardColor color, final CardValue value) {
        this.color = color;
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeEffect(final Game game) {
        // Intentionally empty.
        // Numeric cards do not modify game flow (no skips, no draws).
        // The Game controller handles the standard "next turn" progression automatically
        // after this method returns.
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
        return String.format("%s %s", color, value);
    }
}
