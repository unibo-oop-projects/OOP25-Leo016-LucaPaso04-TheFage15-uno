package uno.model.game.impl.states;

import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameState;
import uno.model.game.impl.AbstractGameState;
import uno.model.players.impl.AbstractPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * State representing waiting for a player choice (e.g. for specific Wild
 * cards).
 */
public class WaitingForPlayerState extends AbstractGameState {

    /**
     * Constructor for WaitingForPlayerState.
     * 
     * @param game The game context to which this state belongs.
     */
    public WaitingForPlayerState(final GameContext game) {
        super(game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameState getEnum() {
        return GameState.WAITING_FOR_PLAYER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void chosenPlayer(final AbstractPlayer player) {
        final Card playedCard = this.getGame().getCurrentPlayedCard();

        this.getGame().getLogger().logAction(this.getGame().getCurrentPlayer().getName(), 
            "CHOOSEN_PLAYER", "N/A", player.getName());

        if (playedCard.getValue(this.getGame()) == CardValue.WILD_FORCED_SWAP) {

            final AbstractPlayer currentPlayer = this.getGame().getCurrentPlayer();
            final List<Optional<Card>> tempHand = new ArrayList<>(currentPlayer.getHand());
            currentPlayer.setHand(player.getHand());
            player.setHand(tempHand);
        }

        if (playedCard.getValue(this.getGame()) == CardValue.WILD_TARGETED_DRAW_TWO) {
            this.getGame().drawCardForPlayer(player);
            this.getGame().drawCardForPlayer(player);
        }

        this.getGame().setGameState(new RunningState(this.getGame()));
        this.getGame().notifyObservers();
    }
}
