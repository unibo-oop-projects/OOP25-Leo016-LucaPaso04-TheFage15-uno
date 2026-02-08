package uno.model.game.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameStateBehavior;
import uno.model.players.api.AbstractPlayer;

import java.util.Optional;

/**
 * Abstract base class for Game States.
 * Throws IllegalStateException by default for all actions.
 * Concrete states should override methods that are valid for that state.
 */
public abstract class AbstractGameState implements GameStateBehavior {

    protected final GameContext game;

    public AbstractGameState(GameContext game) {
        this.game = game;
    }

    @Override
    public void playCard(Optional<Card> card) {
        throw new IllegalStateException("Non è possibile giocare una carta in questo stato: " + getEnum());
    }

    @Override
    public void playerInitiatesDraw() {
        throw new IllegalStateException("Non è possibile pescare in questo stato: " + getEnum());
    }

    @Override
    public void playerPassTurn() {
        throw new IllegalStateException("Non è possibile passare il turno in questo stato: " + getEnum());
    }

    @Override
    public void setColor(CardColor color) {
        throw new IllegalStateException("Non è possibile impostare il colore in questo stato: " + getEnum());
    }

    @Override
    public void chosenPlayer(AbstractPlayer player) {
        throw new IllegalStateException("Non è possibile scegliere un giocatore in questo stato: " + getEnum());
    }

    @Override
    public void drawUntilColorChosenCard(CardColor color) {
        throw new IllegalStateException("Non è possibile pescare fino al colore in questo stato: " + getEnum());
    }
}
