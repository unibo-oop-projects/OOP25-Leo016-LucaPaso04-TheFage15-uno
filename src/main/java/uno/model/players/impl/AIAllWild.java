package uno.model.players.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.api.GameState;

/**
 * AI implementation for UNO All Wild.
 * Focuses on aggressive targeting and strategic use of "Forced Swap".
 */
public class AIAllWild extends AbstractAIPlayer {

    /**
     * Constructor for AIAllWild.
     * 
     * @param name The name of the player.
     */
    public AIAllWild(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void takeTurn(final Game game) {
        // Standard turn logic handled by AbstractAIPlayer
        super.takeTurn(game);

        // if we are waiting for a player choice, we need to choose the target
        if (game.getGameState() == GameState.WAITING_FOR_PLAYER) {
            findBestTarget(game).ifPresent(target -> {
                game.chosenPlayer(target);
                game.aiAdvanceTurn();
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Card> chooseCardToPlay(final Game game) {
        final List<Card> hand = getPlayableCards(game);

        if (hand.isEmpty()) {
            return Optional.empty();
        }

        // Choose the best target (the player with the fewest cards)
        final Optional<AbstractPlayer> bestTargetOpt = findBestTarget(game);

        // Swap Logic
        final Optional<Card> swapCard = hand.stream()
                .filter(c -> c.getValue(game) == CardValue.WILD_FORCED_SWAP)
                .findFirst();

        if (swapCard.isPresent() && bestTargetOpt.isPresent() && this.getHandSize() > bestTargetOpt.get().getHandSize()) {
            return swapCard;
        }

        // Attack Logic (aggressive cards)
        final Optional<Card> attackCard = hand.stream()
                .filter(c -> isAggressiveCard(c.getValue(game)))
                .findFirst();

        if (attackCard.isPresent()) {
            return attackCard;
        }

        // Otherwise, play any non-swap card. If I have only swap cards, I'll be forced to play one of them.
        for (final Card card : hand) {
            boolean isBadSwap = false;
            if (card.getValue(game) == CardValue.WILD_FORCED_SWAP && bestTargetOpt.isPresent() 
                && this.getHandSize() < bestTargetOpt.get().getHandSize()) {
                isBadSwap = true;
            }
            if (!isBadSwap) {
                return Optional.of(card);
            }
        }

        return Optional.of(hand.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CardColor chooseBestColor(final Game game) {
        return CardColor.WILD;
    }

    /**
     * Find the best target player (the one with the fewest cards).
     * 
     * @param game Current game instance.
     * @return The best target player.
     */
    private Optional<AbstractPlayer> findBestTarget(final Game game) {
        return game.getPlayers().stream()
                .filter(p -> !p.equals(this))
                .min(Comparator.comparingInt(AbstractPlayer::getHandSize));
    }

    /**
     * Check if the card is an aggressive type.
     * 
     * @param val Card value to check.
     * @return true if aggressive.
     */
    private boolean isAggressiveCard(final CardValue val) {
        return val == CardValue.WILD_TARGETED_DRAW_TWO
                || val == CardValue.WILD_DRAW_FOUR_ALLWILD
                || val == CardValue.WILD_DRAW_TWO_ALLWILD
                || val == CardValue.WILD_SKIP_TWO
                || val == CardValue.WILD_SKIP;
    }

    /**
     * Get playable cards from hand.
     * 
     * @param game Current game instance.
     * @return List of playable cards.
     */
    private List<Card> getPlayableCards(final Game game) {
        final List<Card> list = new ArrayList<>();
        for (final Optional<Card> opt : getHand()) {
            if (opt.isPresent() && isMoveValid(opt.get(), game)) {
                list.add(opt.get());
            }
        }
        return list;
    }
}
