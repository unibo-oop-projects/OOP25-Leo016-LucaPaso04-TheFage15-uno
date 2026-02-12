package uno.model.game.impl.states;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameState;
import uno.model.game.impl.AbstractGameState;
import uno.model.players.impl.AbstractPlayer;

import java.util.Optional;

/**
 * State representing waiting for a color choice (e.g. after Wild card).
 */
public class WaitingForColorState extends AbstractGameState {

    /**
     * Constructor for WaitingForColorState.
     * 
     * @param game The game context to which this state belongs.
     */
    public WaitingForColorState(final GameContext game) {
        super(game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameState getEnum() {
        return GameState.WAITING_FOR_COLOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(final CardColor color) {
        final Card playedCard = this.getGame().getCurrentPlayedCard();

        this.getGame().getLogger().logAction(this.getGame().getCurrentPlayer().getName(), 
            "SET_COLOR", "N/A", color.toString());

        if (playedCard.getValue(this.getGame()) == CardValue.WILD_DRAW_COLOR) {
            drawUntilColorChosenCard(color);
            return;
        }

        this.getGame().setCurrentColorOptional(Optional.of(color));
        this.getGame().setGameState(new RunningState(this.getGame()));
        this.getGame().notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawUntilColorChosenCard(final CardColor color) {
        final AbstractPlayer nextPlayer = this.getGame().getTurnManager().peekNextPlayer();

        while (true) {
            final Optional<Card> drawnCard = this.getGame().getDrawDeck().draw();
            if (drawnCard.isPresent()) {
                nextPlayer.addCardToHand(drawnCard.get());

                if (drawnCard.get().getColor(this.getGame()) == color) {
                    break;
                }
            }
        }

        this.getGame().setCurrentColorOptional(Optional.of(color));
        this.getGame().setGameState(new RunningState(this.getGame()));
        this.getGame().getTurnManager().advanceTurn(this.getGame());
        this.getGame().notifyObservers();
    }
}
