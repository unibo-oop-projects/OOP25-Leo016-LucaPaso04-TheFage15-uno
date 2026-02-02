package uno.model.cards.behaviors.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.game.api.Game;

/**
 * Implementation of {@link CardSideBehavior} for penalty cards (Draw Two, Wild Draw Four, etc.).
 * This behavior encapsulates the logic of forcing the next player to draw a specific
 * amount of cards and subsequently forfeiting their turn (standard UNO rules).
 */
public class DrawBehavior implements CardSideBehavior {

    private final CardColor color;
    private final CardValue value;
    private final int amountToDraw;

    /**
     * Constructs a behavior that forces a draw action.
     * @param color        The color of the card side.
     * @param value        The face value (e.g., DRAW_TWO, WILD_DRAW_FOUR).
     * @param amountToDraw The number of cards the victim must draw (e.g., 2, 4, 5).
     */
    public DrawBehavior(final CardColor color, final CardValue value, final int amountToDraw) {
        this.color = color;
        this.value = value;
        this.amountToDraw = amountToDraw;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeEffect(final Game game) {
        game.makeNextPlayerDraw(amountToDraw);
        game.skipPlayers(1);
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
     * Specific getter for this behavior.
     * Useful for AI evaluation (e.g., determining threat level) or scoring.
     * @return The number of cards this behavior forces to draw.
     */
    public int getDrawAmount() {
        return amountToDraw;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s %s (+%d)", color, value, amountToDraw);
    }
}
