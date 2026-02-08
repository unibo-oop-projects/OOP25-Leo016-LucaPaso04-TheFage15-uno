package uno.model.game.impl;

import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.MoveValidator;
import uno.model.players.api.AbstractPlayer;

import java.util.Optional;

/**
 * Concrete implementation of MoveValidator.
 */
public class MoveValidatorImpl implements MoveValidator {

    private final GameContext gameContext;

    public MoveValidatorImpl(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    @Override
    public boolean isValidMove(final Card cardToPlay) {
        final Optional<Card> topCard = gameContext.getTopDiscardCard();
        return topCard.isPresent() && cardToPlay.canBePlayedOn(topCard.get(), gameContext);
    }

    @Override
    public boolean playerHasPlayableCard(final AbstractPlayer player) {
        for (final Optional<Card> card : player.getHand()) {
            if (card.isPresent() && isValidMove(card.get())) {
                return true;
            }
        }
        return false;
    }
}
