package uno.model.game.impl.states;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameState;
import uno.model.game.impl.AbstractGameState;
import uno.model.players.api.AbstractPlayer;

import java.util.Optional;

/**
 * State representing waiting for a color choice (e.g. after Wild card).
 */
public class WaitingForColorState extends AbstractGameState {

    public WaitingForColorState(GameContext game) {
        super(game);
    }

    @Override
    public GameState getEnum() {
        return GameState.WAITING_FOR_COLOR;
    }

    @Override
    public void setColor(CardColor color) {
        // Deve prendere il valore della carta giocata
        final Card playedCard = game.getCurrentPlayedCard();

        game.getLogger().logAction(game.getCurrentPlayer().getName(), "SET_COLOR", "N/A", color.toString());

        if (playedCard.getValue(game) == CardValue.WILD_DRAW_COLOR) {
            drawUntilColorChosenCard(color);
            return;
        }

        game.setCurrentColorOptional(Optional.of(color));
        game.setGameState(new RunningState(game)); // Transition back to Running

        game.notifyObservers();
    }

    @Override
    public void drawUntilColorChosenCard(CardColor color) {
        final AbstractPlayer nextPlayer = game.getTurnManager().peekNextPlayer();

        while (true) {
            final Optional<Card> drawnCard = game.getDrawDeck().draw();
            if (drawnCard.isPresent()) {
                nextPlayer.addCardToHand(drawnCard.get());

                if (drawnCard.get().getColor(game) == color) {
                    break;
                }
            }
        }

        game.setCurrentColorOptional(Optional.of(color));

        game.setGameState(new RunningState(game)); // Transition back to Running
        game.getTurnManager().advanceTurn(game);
        game.notifyObservers();
    }
}
