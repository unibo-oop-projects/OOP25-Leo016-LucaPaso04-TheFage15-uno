package uno.model.game.impl;

import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.DeckHandler;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameRules;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;

import java.util.List;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Concrete implementation of DeckHandler.
 */
public class DeckHandlerImpl implements DeckHandler {

    private final Deck<Card> drawDeck;
    private final DiscardPile discardPile;
    private final GameRules rules;
    private final GameLogger logger;
    private final String loggerPlayerName;

    /**
     * Constructor for DeckHandlerImpl.
     * 
     * @param drawDeck the draw deck.
     * @param discardPile discard pile.
     * @param rules selected game rules.
     * @param logger game logger for logging actions.
     * @param loggerPlayerName the name of the player for logging purposes.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public DeckHandlerImpl(final Deck<Card> drawDeck, final DiscardPile discardPile, final GameRules rules, 
            final GameLogger logger, final String loggerPlayerName) {
        this.drawDeck = drawDeck;
        this.discardPile = discardPile;
        this.rules = rules;
        this.logger = logger;
        this.loggerPlayerName = loggerPlayerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean drawCardForPlayer(final AbstractPlayer player, final GameContext game) {
        if (drawDeck.isEmpty()) {
            if (rules.isMandatoryPassEnabled()) {
                logger.logAction(loggerPlayerName, "DECK_EMPTY", "N/A", "No Reshuffle Rule Active. Game Ends.");
                return false;
            }

            reshuffleDiscardPile();
        }

        final Optional<Card> drawnCard = drawDeck.draw();
        if (drawnCard.isPresent()) {
            player.addCardToHand(drawnCard.get());
        }

        logger.logAction(player.getName(), "DRAW",
                drawnCard.isPresent() ? drawnCard.get().getClass().getSimpleName() : "NONE",
                drawnCard.isPresent() ? drawnCard.get().getValue(game).toString() : "NONE");

        return true;
    }

    /**
     * Reshuffles the discard pile back into the draw deck, leaving the top card of the discard pile intact.
     */
    private void reshuffleDiscardPile() {
        final List<Card> cardsToReshuffle = discardPile.takeAllExceptTop();

        if (cardsToReshuffle.isEmpty()) {
            return;
        }
        for (final Card card : cardsToReshuffle) {
            drawDeck.addCard(card);
        }
        drawDeck.shuffle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Deck<Card> getDrawDeck() {
        return drawDeck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public DiscardPile getDiscardPile() {
        return discardPile;
    }
}
