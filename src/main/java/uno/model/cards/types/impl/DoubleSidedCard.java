package uno.model.cards.types.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;

import java.util.Objects;

/**
 * Implementation of a generic UNO card that possesses two sides (Light and
 * Dark).
 * This class acts as a <b>Proxy</b>: it holds two {@link CardSideBehavior}
 * strategies
 * and delegates all calls (getColor, getValue, performEffect) to the currently
 * active side based on the {@link GameImpl#isDarkSide()} state.
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
        // Ensure behaviors are never null to avoid runtime NPEs
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
        final CardSideBehavior myFace = getActiveSide(game);

        // FIX: Gestione sicura del Colore.
        // 1. Prova a prendere il colore attivo dal gioco (es. impostato da un Jolly).
        // 2. Se è vuoto (Optional.empty), usa il colore fisico della carta in cima agli
        // scarti.
        // Questo evita NoSuchElementException e gestisce lo stato transitorio.
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
        // Delegates execution to the active strategy (e.g., Draw, Skip, Flip)
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
