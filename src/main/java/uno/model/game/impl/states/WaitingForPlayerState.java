package uno.model.game.impl.states;

import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameState;
import uno.model.game.impl.AbstractGameState;
import uno.model.players.api.AbstractPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * State representing waiting for a player choice (e.g. for specific Wild
 * cards).
 */
public class WaitingForPlayerState extends AbstractGameState {

    public WaitingForPlayerState(GameContext game) {
        super(game);
    }

    @Override
    public GameState getEnum() {
        return GameState.WAITING_FOR_PLAYER;
    }

    @Override
    public void chosenPlayer(AbstractPlayer player) {
        final Card playedCard = game.getCurrentPlayedCard();

        game.getLogger().logAction(game.getCurrentPlayer().getName(), "CHOOSEN_PLAYER", "N/A", player.getName());

        if (playedCard.getValue(game) == CardValue.WILD_FORCED_SWAP) {

            final AbstractPlayer currentPlayer = game.getCurrentPlayer();

            // Scambia le mani
            final List<Optional<Card>> tempHand = new ArrayList<>(currentPlayer.getHand());
            currentPlayer.setHand(player.getHand());
            player.setHand(tempHand);
        }

        if (playedCard.getValue(game) == CardValue.WILD_TARGETED_DRAW_TWO) {
            game.drawCardForPlayer(player);
            game.drawCardForPlayer(player);
        }

        game.setGameState(new RunningState(game)); // Transition back to Running

        game.notifyObservers();
    }
}
