package uno.model.game.impl;

import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.MoveValidator;
import uno.model.players.impl.AbstractPlayer;

import java.util.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Concrete implementation of MoveValidator.
 */
public class MoveValidatorImpl implements MoveValidator {

    private final GameContext gameContext;

    /**
     * Constructor for MoveValidatorImpl.
     * 
     * @param gameContext the game context to be used for validating moves.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public MoveValidatorImpl(final GameContext gameContext) {
        this.gameContext = gameContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidMove(final Card cardToPlay) {
        final Optional<Card> topCard = gameContext.getTopDiscardCard();
        return topCard.isPresent() && cardToPlay.canBePlayedOn(topCard.get(), gameContext);
    }

    /**
     * {@inheritDoc}
     */
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
