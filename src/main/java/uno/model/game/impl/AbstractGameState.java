package uno.model.game.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameStateBehavior;
import uno.model.players.impl.AbstractPlayer;

import java.util.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Abstract base class for Game States.
 * Throws IllegalStateException by default for all actions.
 * Concrete states should override methods that are valid for that state.
 */
public abstract class AbstractGameState implements GameStateBehavior {

    private final GameContext game;

    /**
     * Constructor to initialize the game context.
     * 
     * @param game the game context to be used by the state.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AbstractGameState(final GameContext game) {
        this.game = game;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playCard(final Optional<Card> card) {
        throw new IllegalStateException("Cannot play a card in this state: " + getEnum());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playerInitiatesDraw() {
        throw new IllegalStateException("Cannot draw a card in this state: " + getEnum());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playerPassTurn() {
        throw new IllegalStateException("Cannot pass turn in this state: " + getEnum());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(final CardColor color) {
        throw new IllegalStateException("Cannot set color in this state: " + getEnum());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void chosenPlayer(final AbstractPlayer player) {
        throw new IllegalStateException("Cannot choose a player in this state: " + getEnum());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawUntilColorChosenCard(final CardColor color) {
        throw new IllegalStateException("Cannot draw until color chosen in this state: " + getEnum());
    }

    /**
     * Getter for the game context.
     * 
     * @return the game context associated with this state.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public GameContext getGame() {
        return this.game;
    }
}
