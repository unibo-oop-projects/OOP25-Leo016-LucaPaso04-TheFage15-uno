package uno.model.cards.types.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;

import java.util.Objects;

/**
 * Implementation of a generic UNO card that possesses two sides (Light and
 * Dark).
 * This class uses the Strategy design pattern to delegate behavior based on the
 * current game state (Light Mode or Dark Mode).
 * Each side can have its own color, value, point value, and special effects.
 */
public class DoubleSidedCard implements Card {

    private final CardSideBehavior lightSide;
    private final CardSideBehavior darkSide;

    /**
     * Constructs a card with two distinct behaviors.
     * 
     * @param lightSide The behavior when the game is in Light Mode.
     * @param darkSide  The behavior when the game is in Dark Mode.
     */
    public DoubleSidedCard(final CardSideBehavior lightSide, final CardSideBehavior darkSide) {
        this.lightSide = Objects.requireNonNull(lightSide, "Light side behavior cannot be null");
        this.darkSide = Objects.requireNonNull(darkSide, "Dark side behavior cannot be null");
    }

    /**
     * Helper method to determine which behavior is currently active.
     * 
     * @param game The current game context.
     * @return The active {@link CardSideBehavior} based on the game's side state.
     */
    private CardSideBehavior getActiveSide(final Game game) {
        return game.isDarkSide() ? darkSide : lightSide;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardColor getColor(final Game game) {
        return getActiveSide(game).getColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardValue getValue(final Game game) {
        return getActiveSide(game).getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPointValue(final Game game) {
        return getActiveSide(game).getPointValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWild(final Game game) {
        return getActiveSide(game).isWild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBePlayedOn(final Card topCard, final Game game) {
        final CardSideBehavior myFace = this.getActiveSide(game);

        final CardColor targetColor = game.getCurrentColor()
                .orElse(topCard.getColor(game));

        return myFace.getColor() == CardColor.WILD || myFace.getColor() == targetColor
                || myFace.getValue() == topCard.getValue(game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performEffect(final Game game) {
        getActiveSide(game).executeEffect(game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Card[Light=%s, Dark=%s]", lightSide, darkSide);
    }
}
